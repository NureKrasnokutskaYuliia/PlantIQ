import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageSwitcher, ThemeToggle } from '../../components/index';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, LanguageSwitcher, ThemeToggle, TranslateModule],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {
  private auth = inject(AuthService);
  private router = inject(Router);

  get userName(): string {
    return localStorage.getItem('userName') ?? 'Admin';
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
