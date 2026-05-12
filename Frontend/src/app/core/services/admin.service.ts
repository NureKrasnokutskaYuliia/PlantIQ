import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService, API_BASE_URL } from './api.service';
import { UserResponse } from '../models/auth.model';
import { SystemStats } from '../models/admin.model';

@Injectable({ providedIn: 'root' })
export class AdminService extends ApiService {
  getStats(): Observable<SystemStats> {
    return this.http.get<SystemStats>(`${API_BASE_URL}/Admin/stats`, { headers: this.headers });
  }
  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${API_BASE_URL}/Users`, { headers: this.headers });
  }
  createUser(dto: any): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${API_BASE_URL}/Users`, dto, { headers: this.headers });
  }
  banUser(id: number): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/Admin/users/${id}/ban`, {}, { headers: this.headers });
  }
  unbanUser(id: number): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/Admin/users/${id}/unban`, {}, { headers: this.headers });
  }
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/Users/${id}`, { headers: this.headers });
  }
  updateUserRole(id: number, dto: any): Observable<void> {
    return this.http.put<void>(`${API_BASE_URL}/Users/${id}`, dto, { headers: this.headers });
  }
  getSettings(): Observable<any[]> {
    return this.http.get<any[]>(`${API_BASE_URL}/SystemSettings`, { headers: this.headers });
  }
  updateSetting(key: string, value: string): Observable<void> {
    return this.http.put<void>(`${API_BASE_URL}/SystemSettings/${key}`, { key, value }, { headers: this.headers });
  }
  sendGlobalNotification(title: string, message: string): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/Admin/notifications/global`, { title, message }, { headers: this.headers });
  }
  exportData(): object {
    // Build export by collecting from storage
    return {
      exportedAt: new Date().toISOString(),
      settings: 'Use /api/SystemSettings endpoint',
    };
  }
}
