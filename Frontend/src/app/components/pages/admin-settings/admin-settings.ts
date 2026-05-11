import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './admin-settings.html',
  styleUrl: './admin-settings.scss'
})
export class AdminSettings implements OnInit {
  private admin = inject(AdminService);
  settings = signal<any[]>([]);
  isLoading = signal(true);
  success = signal('');
  error = signal('');
  editValues: Record<string, string> = {};

  ngOnInit() {
    this.admin.getSettings().subscribe({
      next: (s) => {
        this.settings.set(s);
        s.forEach(item => this.editValues[item.key] = item.value);
        this.isLoading.set(false);
      },
      error: () => { this.error.set('COMMON.ERRORS.LOAD_FAILED'); this.isLoading.set(false); }
    });
  }

  save(key: string) {
    this.admin.updateSetting(key, this.editValues[key]).subscribe({
      next: () => this.success.set(`Налаштування "${key}" збережено.`),
      error: () => this.error.set(`Помилка при збереженні "${key}".`)
    });
  }
}
