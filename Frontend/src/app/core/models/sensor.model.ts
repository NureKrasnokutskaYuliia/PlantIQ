export interface SensorData {
  dataId: number;
  plantId: number;
  deviceId: number;
  timestamp: string;
  soilMoisture?: number;
  lightIntensity?: number;
  batteryLevel?: number;
}

export interface WateringEvent {
  eventId: number;
  plantId: number;
  scheduleId?: number;
  timestamp: string;
  amountMl: number;
  mode: number;
  status: number;
  notes?: string;
}

export interface WateringSchedule {
  scheduleId: number;
  plantId: number;
  startTime: string;
  intervalHours: number;
  amountMl: number;
  enabled: boolean;
  daysOfWeek: number[];
}
