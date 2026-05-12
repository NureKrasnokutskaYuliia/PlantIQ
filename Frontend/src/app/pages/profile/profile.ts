import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ApiService, API_BASE_URL } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

interface UserProfile {
  userId: number;
  name: string;
  email: string;
  role: number;
  createdAt: string;
  isActive: boolean;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, DatePipe],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile extends ApiService implements OnInit {
  private authService = inject(AuthService);

  isLoading = signal(true);
  isSaving = signal(false);
  error = signal('');
  success = signal('');

  // Editable fields
  name = '';
  email = '';

  // Read-only info
  userId = 0;
  role = 0;
  createdAt = '';
  isActive = true;
  selectedTimezone = 'UTC+2 (Kyiv)';
  timezones = [
    'UTC-5 (New York)', 'UTC+0 (London)', 'UTC+1 (Berlin)', 'UTC+2 (Kyiv)', 'UTC+3 (Istanbul)', 'UTC+9 (Tokyo)'
  ];
  isLocalhost = signal(window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1');

  ngOnInit() {
    const id = Number(localStorage.getItem('userId'));
    if (!id) { this.error.set('COMMON.ERRORS.USER_NOT_FOUND'); this.isLoading.set(false); return; }
    this.userId = id;

    this.http.get<UserProfile>(`${API_BASE_URL}/Users/${id}`, { headers: this.headers }).subscribe({
      next: (u) => {
        this.name = u.name;
        this.email = u.email;
        this.role = u.role;
        this.createdAt = u.createdAt;
        this.isActive = u.isActive;
        this.selectedTimezone = localStorage.getItem('userTimezone') || 'UTC+2 (Kyiv)';
        this.isLoading.set(false);
      },
      error: () => {
        // Fallback to localStorage if API fails
        this.name = localStorage.getItem('userName') ?? '';
        this.email = '';
        this.isLoading.set(false);
        this.error.set('COMMON.ERRORS.LOAD_FAILED');
      }
    });
  }

  save() {
    if (!this.name.trim() || !this.email.trim()) {
      this.error.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    if (!this.email.includes('@')) {
      this.error.set('COMMON.ERRORS.INVALID_EMAIL');
      return;
    }

    this.isSaving.set(true);
    this.error.set('');
    this.success.set('');

    const dto = { name: this.name, email: this.email, role: this.role, isActive: this.isActive };

    this.http.put(`${API_BASE_URL}/Users/${this.userId}`, dto, { headers: this.headers }).subscribe({
      next: () => {
        this.isSaving.set(false);
        this.success.set('PROFILE.SAVE_SUCCESS');
        // Update localStorage
        localStorage.setItem('userName', this.name);
        localStorage.setItem('userTimezone', this.selectedTimezone);
      },
      error: (err) => {
        this.isSaving.set(false);
        if (err.status === 409) {
          this.error.set('COMMON.ERRORS.EMAIL_EXISTS');
        } else {
          this.error.set('COMMON.ERRORS.GENERAL');
        }
      }
    });
  }

  getRoleKey(): string {
    return this.role === 1 ? 'PROFILE.ROLE_ADMIN' : 'PROFILE.ROLE_USER';
  }

  deleteAccount() {
    if (!confirm('УВАГА! Ви справді хочете видалити свій акаунт? Усі ваші дані (рослини, пристрої, розклади) будуть безповоротно видалені.')) return;
    if (!confirm('Підтвердіть ще раз: видалити акаунт назавжди?')) return;
    this.http.delete(`${API_BASE_URL}/Users/${this.userId}`, { headers: this.headers }).subscribe({
      next: () => {
        this.authService.logout();
        window.location.href = '/login';
      },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

}
