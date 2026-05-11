import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, API_BASE_URL } from './api.service';

export interface Plant {
  plantId: number;
  userId: number;
  deviceId?: number;
  name: string;
  species?: string;
  optimalMoistureMin?: number;
  optimalMoistureMax?: number;
  optimalLightMin?: number;
  optimalLightMax?: number;
  notes?: string;
  createdAt: string;
  isActive: boolean;
  wateringMode: number;
}

export interface CreatePlantDto {
  name: string;
  deviceId?: number;
  species?: string;
  optimalMoistureMin?: number;
  optimalMoistureMax?: number;
  optimalLightMin?: number;
  optimalLightMax?: number;
  notes?: string;
  wateringMode: number;
}

@Injectable({ providedIn: 'root' })
export class PlantService extends ApiService {
  getMyPlants(): Observable<Plant[]> {
    return this.http.get<Plant[]>(`${API_BASE_URL}/Plants/User`, { headers: this.headers });
  }
  getPlant(id: number): Observable<Plant> {
    return this.http.get<Plant>(`${API_BASE_URL}/Plants/${id}`, { headers: this.headers });
  }
  createPlant(dto: CreatePlantDto): Observable<Plant> {
    return this.http.post<Plant>(`${API_BASE_URL}/Plants`, dto, { headers: this.headers });
  }
  updatePlant(id: number, dto: any): Observable<void> {
    return this.http.put<void>(`${API_BASE_URL}/Plants/${id}`, dto, { headers: this.headers });
  }
  deletePlant(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/Plants/${id}`, { headers: this.headers });
  }
}
