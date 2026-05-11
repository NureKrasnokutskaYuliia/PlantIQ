import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-notifications',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './admin-notifications.html',
  styleUrl: './admin-notifications.scss'
})
export class AdminNotifications {
  private admin = inject(AdminService);

  title = '';
  message = '';
  isSending = signal(false);
  success = signal('');
  error = signal('');

  send() {
    if (!this.title.trim() || !this.message.trim()) {
      this.error.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    this.isSending.set(true);
    this.error.set('');
    this.success.set('');

    this.admin.sendGlobalNotification(this.title, this.message).subscribe({
      next: () => {
        this.isSending.set(false);
        this.success.set('✅ Сповіщення успішно надіслано всім користувачам!');
        this.title = '';
        this.message = '';
      },
      error: () => {
        this.isSending.set(false);
        this.error.set('COMMON.ERRORS.GENERAL');
      }
    });
  }
}
