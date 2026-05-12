export interface Device {
  deviceId: number;
  userId: number;
  name: string;
  model?: string;
  status: number; // 0=Online,1=Offline,2=Error
  firmwareVersion?: string;
  lastSync?: string;
  serialNumber?: string;
  createdAt: string;
}

export interface CreateDeviceDto {
  name: string;
  model?: string;
  serialNumber?: string;
}
