import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, API_BASE_URL } from './api.service';

import { Device, CreateDeviceDto } from '../models/device.model';

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
