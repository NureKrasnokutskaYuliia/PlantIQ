import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService } from '../../../services/admin.service';
import { UserResponse } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-export',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './admin-export.html',
  styleUrl: './admin-export.scss'
})
export class AdminExport implements OnInit {
  private admin = inject(AdminService);
  users = signal<UserResponse[]>([]);
  settings = signal<any[]>([]);
  stats = signal<any>(null);
  isExporting = signal(false);

  ngOnInit() {
    this.admin.getAllUsers().subscribe({ next: (u) => this.users.set(u) });
    this.admin.getSettings().subscribe({ next: (s) => this.settings.set(s) });
    this.admin.getStats().subscribe({ next: (s) => this.stats.set(s) });
  }

  exportJson() {
    this.isExporting.set(true);
    const data = {
      exportedAt: new Date().toISOString(),
      stats: this.stats(),
      users: this.users(),
      settings: this.settings()
    };
    this.download(JSON.stringify(data, null, 2), `plantiq-backup-${this.dateStr()}.json`, 'application/json');
    setTimeout(() => this.isExporting.set(false), 500);
  }

  exportUsersCsv() {
    const rows = [['ID', 'Ім\'я', 'Email', 'Роль', 'Активний', 'Останній вхід', 'Дата реєстрації']];
    this.users().forEach(u => rows.push([
      String(u.userId), u.name, u.email,
      u.role === 1 ? 'Адмін' : 'Користувач',
      u.isActive ? 'Так' : 'Ні',
      u.lastLogin ?? '—',
      u.createdAt
    ]));
    const csv = '\uFEFF' + rows.map(r => r.map(v => `"${v}"`).join(',')).join('\n');
    this.download(csv, `plantiq-users-${this.dateStr()}.csv`, 'text/csv;charset=utf-8;');
  }

  exportSettingsCsv() {
    const rows = [['Ключ', 'Значення']];
    this.settings().forEach(s => rows.push([s.key, s.value ?? '']));
    const csv = '\uFEFF' + rows.map(r => r.map(v => `"${v}"`).join(',')).join('\n');
    this.download(csv, `plantiq-settings-${this.dateStr()}.csv`, 'text/csv;charset=utf-8;');
  }

  private download(content: string, filename: string, type: string) {
    const blob = new Blob([content], { type });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.click();
    URL.revokeObjectURL(a.href);
  }

  private dateStr() {
    return new Date().toISOString().slice(0, 10);
  }
}
