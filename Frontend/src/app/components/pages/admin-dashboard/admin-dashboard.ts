import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AdminService, SystemStats } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslateModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit {
  private adminService = inject(AdminService);

  stats = signal<SystemStats | null>(null);
  isLoading = signal(true);
  error = signal('');

  ngOnInit() {
    this.adminService.getStats().subscribe({
      next: (s) => { this.stats.set(s); this.isLoading.set(false); },
      error: () => { this.error.set('COMMON.ERRORS.LOAD_FAILED'); this.isLoading.set(false); }
    });
  }
}
