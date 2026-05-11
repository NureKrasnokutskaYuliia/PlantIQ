import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageSwitcher, ThemeToggle } from '../../../shared/index';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LanguageSwitcher, ThemeToggle],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private router = inject(Router);
  private authService = inject(AuthService);

  email = '';
  password = '';
  isLoading = signal(false);
  errorMessage = signal('');

  login() {
    this.errorMessage.set('');

    if (!this.email || !this.password) {
      this.errorMessage.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    if (!this.email.includes('@')) {
      this.errorMessage.set('COMMON.ERRORS.INVALID_EMAIL');
      return;
    }

    this.isLoading.set(true);

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (resp) => {
        this.authService.saveSession(resp);
        this.isLoading.set(false);

        if (this.authService.isAdmin()) {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/user/dashboard']);
        }
      },
      error: (err: Error) => {
        this.errorMessage.set(err.message);
        this.isLoading.set(false);
      }
    });
  }
}
