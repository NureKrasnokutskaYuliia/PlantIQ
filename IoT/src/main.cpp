#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <vector>
#include "time.h"

const char* ssid = WIFI_SSID;
const char* password = WIFI_PASS;
const char* baseUrl = API_BASE_URL;
const char* apiToken = API_TOKEN;
const int plantId = PLANT_ID;
const int deviceId = DEVICE_ID;

const int pumpPin = 18;
const int moisturePin = 34;
const int lightPin = 35;
const int batteryPin = 32;

struct SensorReadings {
    double soilMoisture;
    double lightIntensity;
    double batteryLevel;
};

struct Schedule {
    int id;
    int hour;
    int minute;
    int amountMl;
    bool days[7]; 
    bool watered = false; 
};

std::vector<Schedule> activeSchedules;

void log(const char* tag, String message) {
    struct tm timeinfo;
    if (getLocalTime(&timeinfo)) {
        Serial.printf("[%02d:%02d:%02d] [%s] %s\n", 
                      timeinfo.tm_hour, timeinfo.tm_min, timeinfo.tm_sec, 
                      tag, message.c_str());
    } else {
        Serial.printf("[%lu] [%s] %s\n", millis(), tag, message.c_str());
    }
}

void fetchSchedules() {
    if (WiFi.status() != WL_CONNECTED) {
      log("[ERROR]", "WiFi not connected. Skipping schedule fetch.");
      return;
    }

    log("[API]", "Fetching schedules from: " + String(baseUrl));
    HTTPClient http;
    http.begin(String(baseUrl) + "/WateringSchedules/Plant/" + String(plantId));

    int httpCode = http.GET();
    if (httpCode == HTTP_CODE_OK) {
        String payload = http.getString();
        DynamicJsonDocument doc(4096);
        DeserializationError error = deserializeJson(doc, payload);

        if (error) {
            log("[ERROR]", "JSON Parse failed: " + String(error.c_str()));
            return;
        }

        JsonArray arr = doc.as<JsonArray>();
        activeSchedules.clear();

        for (JsonObject s : arr) {
            if (s["enabled"].as<bool>()) {
                Schedule newSched;
                newSched.id = s["scheduleId"];
                newSched.amountMl = s["amountMl"];

                String timeStr = s["startTime"].as<String>();
                newSched.hour = timeStr.substring(0, 2).toInt();
                newSched.minute = timeStr.substring(3, 5).toInt();
                for(int i=0; i<7; i++) newSched.days[i] = false;
                JsonArray days = s["daysOfWeek"].as<JsonArray>();
                for (int day : days) {
                    if (day >= 0 && day <= 6) newSched.days[day] = true;
                }
                
                activeSchedules.push_back(newSched);
            }
        }
        log("[API]", "Update successful. Active schedules: " + String(activeSchedules.size()));
    } else {
        log("[ERROR]", "Fetch schedules failed, HTTP code: " + String(httpCode));
    }
    http.end();
}

void postWateringEvent(int scheduleId, int amount, String status) {
    if (WiFi.status() != WL_CONNECTED) return;
    log("[API]", "Reporting watering event (Schedule ID: " + String(scheduleId) + ")");

    HTTPClient http;
    http.begin(String(baseUrl) + "/WateringEvents");
    http.addHeader("Content-Type", "application/json");
    http.addHeader("Authorization", "Bearer " + String(apiToken));

    StaticJsonDocument<256> doc;
    doc["plantId"] = plantId;
    doc["scheduleId"] = scheduleId;
    doc["amountMl"] = amount;
    doc["mode"] = "Scheduled";
    doc["status"] = status;    

    String requestBody;
    serializeJson(doc, requestBody);

    int httpCode = http.POST(requestBody);
    if (httpCode >= 200 && httpCode < 300) {
        log("[API]", "Watering event logged successfully (201 Created)");
    } else {
        log("[ERROR]", "Failed to log event. HTTP: " + String(httpCode));
    }
    http.end();
}

void performWatering(Schedule &s) {
    log("[PUMP]", "START: Watering Schedule " + String(s.id) + " | Amount: " + String(s.amountMl) + "ml");

    digitalWrite(pumpPin, HIGH);
    int durationMs = (s.amountMl / 20) * 1000; 
    delay(durationMs); 
    digitalWrite(pumpPin, LOW);

    log("[PUMP]", "STOP: Watering finished after " + String(durationMs) + "ms");
    postWateringEvent(s.id, s.amountMl, "Completed");
}

void checkSchedulesLogic() {
    struct tm timeinfo;
    if (!getLocalTime(&timeinfo)) return;

    for (Schedule &s : activeSchedules) {
        bool dayMatch = s.days[timeinfo.tm_wday];
        bool timeMatch = (timeinfo.tm_hour == s.hour && timeinfo.tm_min == s.minute);

        if (dayMatch && timeMatch) {
            if (!s.watered) {
                log("[LOGIC]", "Schedule match found for " + String(s.id) + " | " + String(s.hour) + ":" + String(s.minute));
                performWatering(s);
                s.watered = true; 
            }
        } else {
            s.watered = false; 
            log("LOGIC", "Resetting 'watered' flag for Schedule ID " + String(s.id));
        }
    }
}

SensorReadings readSensors() {
    SensorReadings data;

    int rawMoisture = analogRead(moisturePin);
    data.soilMoisture = map(rawMoisture, 4095, 1500, 0, 100); 

    int rawLight = analogRead(lightPin);
    data.lightIntensity = map(rawLight, 0, 4095, 0, 1000); 

    int rawBattery = analogRead(batteryPin);
    data.batteryLevel = (rawBattery / 4095.0) * 100.0; 

    log("[SENSOR]", "Readings - Moisture: " + String(data.soilMoisture) + "%, Light: " + String(data.lightIntensity));
    return data;
}

void sendSensorData() {
    if (WiFi.status() != WL_CONNECTED) return;

    SensorReadings sensors = readSensors();
    HTTPClient http;

    log("[API]", "Sending telemetry data...");
    http.begin(String(baseUrl) + "/SensorData");
    http.addHeader("Content-Type", "application/json");
    http.addHeader("Authorization", "Bearer " + String(apiToken));

    StaticJsonDocument<256> doc;
    doc["plantId"] = plantId;         
    doc["deviceId"] = deviceId;       
    doc["soilMoisture"] = sensors.soilMoisture;
    doc["lightIntensity"] = sensors.lightIntensity;
    doc["batteryLevel"] = sensors.batteryLevel;

    String jsonBody;
    serializeJson(doc, jsonBody);
    
    int httpCode = http.POST(jsonBody);
    
    if (httpCode == 201) {
        log("[API]", "Telemetry sent. Response: " + String(httpCode));
    } else {
        log("[API]", "Error sending telemetry. Response: " + String(httpCode));
    }
    
    http.end();
}

void setup() {
    Serial.begin(115200);
    pinMode(pumpPin, OUTPUT);
    digitalWrite(pumpPin, LOW);

    Serial.println("\n--- PlantIQ Booting ---");
    log("[WIFI]", "Connecting to " + String(ssid));

    WiFi.begin(ssid, password);
    while (WiFi.status() != WL_CONNECTED) {
      delay(500); 
      Serial.print(".");
    }

    Serial.println();
    log("[WIFI]", "Connected. IP: " + WiFi.localIP().toString());

    log("[TIME]", "Syncing NTP time...");
    configTime(7200, 3600, "pool.ntp.org");
    
    struct tm timeinfo;
    while(!getLocalTime(&timeinfo)){
        delay(500);
    }
    log("[TIME]", "Time synchronized.");

    fetchSchedules(); 
}

unsigned long lastSensorUpdate = 0;
const unsigned long sensorInterval = 600000; 

void loop() {
    checkSchedulesLogic();

    if (millis() - lastSensorUpdate > sensorInterval) {
        sendSensorData();
        lastSensorUpdate = millis();
    }

    static uint32_t lastRefresh = 0;
    if (millis() - lastRefresh > 3600000) {
        fetchSchedules();
        lastRefresh = millis();
    }
    
    delay(1000); 
}