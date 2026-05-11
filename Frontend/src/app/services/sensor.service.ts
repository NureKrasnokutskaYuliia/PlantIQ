import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, API_BASE_URL } from './api.service';

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

@Injectable({ providedIn: 'root' })
export class SensorService extends ApiService {
  getSensorData(plantId: number, limit = 100): Observable<SensorData[]> {
    return this.http.get<SensorData[]>(`${API_BASE_URL}/SensorData/Plant/${plantId}?limit=${limit}`, { headers: this.headers });
  }
}

@Injectable({ providedIn: 'root' })
export class WateringService extends ApiService {
  getEvents(plantId: number): Observable<WateringEvent[]> {
    return this.http.get<WateringEvent[]>(`${API_BASE_URL}/WateringEvents/Plant/${plantId}`, { headers: this.headers });
  }
  getSchedules(plantId: number): Observable<WateringSchedule[]> {
    return this.http.get<WateringSchedule[]>(`${API_BASE_URL}/WateringSchedules/Plant/${plantId}`, { headers: this.headers });
  }
  createSchedule(dto: any): Observable<WateringSchedule> {
    return this.http.post<WateringSchedule>(`${API_BASE_URL}/WateringSchedules`, dto, { headers: this.headers });
  }
  deleteSchedule(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/WateringSchedules/${id}`, { headers: this.headers });
  }
}
