import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, API_BASE_URL } from './api.service';

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

@Injectable({ providedIn: 'root' })
export class DeviceService extends ApiService {
  getMyDevices(): Observable<Device[]> {
    return this.http.get<Device[]>(`${API_BASE_URL}/Devices`, { headers: this.headers });
  }
  getDevice(id: number): Observable<Device> {
    return this.http.get<Device>(`${API_BASE_URL}/Devices/${id}`, { headers: this.headers });
  }
  createDevice(dto: CreateDeviceDto): Observable<Device> {
    return this.http.post<Device>(`${API_BASE_URL}/Devices`, dto, { headers: this.headers });
  }
  updateDevice(id: number, dto: any): Observable<void> {
    return this.http.put<void>(`${API_BASE_URL}/Devices/${id}`, dto, { headers: this.headers });
  }
  deleteDevice(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/Devices/${id}`, { headers: this.headers });
  }
}
