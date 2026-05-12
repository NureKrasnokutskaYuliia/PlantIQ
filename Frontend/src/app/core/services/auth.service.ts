import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

export const API_BASE_URL = 'https://plantiq-mkp3.onrender.com/api';

// ЗМІНИТИ ТУТ: Для продакшену змініть 2 на 30 (хвилин)
export const SESSION_TIMEOUT_MINUTES = 2; 
export const WARNING_BEFORE_SECONDS = 30;

import { LoginRequest, RegisterRequest, UserResponse, LoginResponse } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private baseUrl = API_BASE_URL;

  // Сигнал, який показує модальне вікно попередження
  public showSessionWarning = signal(false);
  private checkInterval: any;

  constructor() {
    this.startSessionTimer();
  }

  private startSessionTimer() {
    if (this.checkInterval) clearInterval(this.checkInterval);
    this.checkInterval = setInterval(() => {
      if (!this.isLoggedIn()) return;
      
      const expiresStr = localStorage.getItem('sessionExpires');
      if (expiresStr) {
        const expires = parseInt(expiresStr, 10);
        const remaining = expires - Date.now();

        if (remaining <= 0) {
          this.logout();
          this.router.navigate(['/login']);
        } else if (remaining <= WARNING_BEFORE_SECONDS * 1000) {
          this.showSessionWarning.set(true);
        } else {
          this.showSessionWarning.set(false);
        }
      }
    }, 5000); // Перевіряємо кожні 5 секунд
  }

  extendSession() {
    const expiresAt = Date.now() + SESSION_TIMEOUT_MINUTES * 60 * 1000;
    localStorage.setItem('sessionExpires', expiresAt.toString());
    this.showSessionWarning.set(false);
  }

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/Users/login`, data).pipe(
      catchError(error => {
        if (error.status === 401 || error.status === 400) {
          return throwError(() => new Error('COMMON.ERRORS.AUTH_FAILED'));
        }
        if (error.status === 0 || error.status === 503) {
          return throwError(() => new Error('COMMON.ERRORS.SERVER_ERROR'));
        }
        return throwError(() => new Error('COMMON.ERRORS.GENERAL'));
      })
    );
  }

  register(data: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}/Users`, data).pipe(
      catchError(error => {
        if (error.status === 409 || error.status === 400) {
          return throwError(() => new Error('COMMON.ERRORS.USER_EXISTS'));
        }
        if (error.status === 0 || error.status === 503) {
          return throwError(() => new Error('COMMON.ERRORS.SERVER_ERROR'));
        }
        return throwError(() => new Error('COMMON.ERRORS.GENERAL'));
      })
    );
  }

  saveSession(resp: LoginResponse) {
    const expiresAt = Date.now() + SESSION_TIMEOUT_MINUTES * 60 * 1000;
    localStorage.setItem('isLoggedIn', 'true');
    localStorage.setItem('token', resp.token);
    
    // ГНУЧКА ПЕРЕВІРКА РОЛІ (число 1 або рядок "Admin")
    const isActuallyAdmin = resp.user.role === 1 || String(resp.user.role).toLowerCase() === 'admin';
    localStorage.setItem('role', isActuallyAdmin ? 'admin' : 'user');
    
    localStorage.setItem('userName', resp.user.name);
    localStorage.setItem('userId', String(resp.user.userId));
    localStorage.setItem('sessionExpires', expiresAt.toString());
    this.showSessionWarning.set(false);
  }

  logout() {
    localStorage.clear();
    this.showSessionWarning.set(false);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    const expiresStr = localStorage.getItem('sessionExpires');
    if (expiresStr) {
      const expires = parseInt(expiresStr, 10);
      if (Date.now() > expires) {
        this.logout();
        return false;
      }
    }
    return localStorage.getItem('isLoggedIn') === 'true';
  }

  isAdmin(): boolean {
    const role = localStorage.getItem('role');
    return role === 'admin' || role === '1';
  }
}
