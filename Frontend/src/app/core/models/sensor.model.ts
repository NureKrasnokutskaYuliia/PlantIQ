export interface SensorData {
  dataId: number;
  plantId: number;
  deviceId: number;
  timestamp: string;
  soilMoisture?: number;
  lightIntensity?: number;
  batteryLevel?: number;
}

export type WateringEventMode = 'Manual' | 'Automatic' | 'Scheduled';
export type WateringEventStatus = 'Completed' | 'Failed' | 'Cancelled';

export interface WateringEvent {
  eventId: number;
  plantId: number;
  scheduleId?: number;
  timestamp: string;
  amountMl: number;
  mode: WateringEventMode;
  status: WateringEventStatus;
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
