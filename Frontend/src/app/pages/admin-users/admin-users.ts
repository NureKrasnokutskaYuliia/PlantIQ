import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AdminService } from '../../core/services/admin.service';
import { UserResponse } from '../../core/models/auth.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, DatePipe, TranslateModule, FormsModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.scss'
})
export class AdminUsers implements OnInit {
  private admin = inject(AdminService);
  private translate = inject(TranslateService);
  users = signal<UserResponse[]>([]);
  isLoading = signal(true);
  error = signal('');
  success = signal('');
  
  // Create user form
  showCreateForm = signal(false);
  newUser = {
    name: '',
    email: '',
    password: '',
    role: 0
  };

  ngOnInit() { this.load(); }

  load() {
    this.isLoading.set(true);
    this.admin.getAllUsers().subscribe({
      next: (u) => { this.users.set(u.sort((a, b) => a.userId - b.userId)); this.isLoading.set(false); },
      error: () => { this.error.set('COMMON.ERRORS.LOAD_FAILED'); this.isLoading.set(false); }
    });
  }

  ban(id: number, name: string) {
    if (!confirm(this.translate.instant('ADMIN.BAN_CONFIRM') + ` (${name})`)) return;
    this.admin.banUser(id).subscribe({
      next: () => { this.success.set('ADMIN.SUCCESS_BAN'); this.load(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  unban(id: number, name: string) {
    if (!confirm(this.translate.instant('ADMIN.UNBAN_CONFIRM') + ` (${name})`)) return;
    this.admin.unbanUser(id).subscribe({
      next: () => { this.success.set('ADMIN.SUCCESS_UNBAN'); this.load(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  delete(id: number, name: string) {
    if (!confirm(this.translate.instant('ADMIN.DELETE_CONFIRM') + ` (${name})`)) return;
    this.admin.deleteUser(id).subscribe({
      next: () => { this.success.set('ADMIN.SUCCESS_DELETE'); this.load(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  promote(id: number, name: string) {
    if (!confirm(this.translate.instant('ADMIN.PROMOTE_CONFIRM') + ` (${name})`)) return;
    const u = this.users().find(user => user.userId === id);
    if (!u) return;
    
    const dto = { name: u.name, email: u.email, role: 1, isActive: u.isActive };
    
    this.admin.updateUserRole(id, dto).subscribe({
      next: () => { this.success.set('ADMIN.SUCCESS_PROMOTE'); this.load(); },
      error: () => this.error.set('COMMON.ERRORS.GENERAL')
    });
  }

  createUser() {
    if (!this.newUser.name || !this.newUser.email || !this.newUser.password) {
      this.error.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    
    this.admin.createUser(this.newUser).subscribe({
      next: () => {
        this.success.set('ADMIN.SUCCESS_CREATE');
        this.showCreateForm.set(false);
        this.newUser = { name: '', email: '', password: '', role: 0 };
        this.load();
      },
      error: (err) => {
        this.error.set(err.status === 409 ? 'COMMON.ERRORS.EMAIL_EXISTS' : 'COMMON.ERRORS.GENERAL');
      }
    });
  }

  getRoleName(role: number) {
    return role === 1 ? '👑 Адмін' : '👤 Користувач';
  }
}
