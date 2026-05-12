import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageSwitcher, ThemeToggle } from '../../../shared/components/index';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LanguageSwitcher, ThemeToggle],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
  private router = inject(Router);
  private authService = inject(AuthService);

  name = '';
  email = '';
  password = '';
  isLoading = signal(false);
  errorMessage = signal('');

  register() {
    this.errorMessage.set('');

    if (!this.name || !this.email || !this.password) {
      this.errorMessage.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    if (!this.email.includes('@')) {
      this.errorMessage.set('COMMON.ERRORS.INVALID_EMAIL');
      return;
    }
    if (this.password.length < 6) {
      this.errorMessage.set('COMMON.ERRORS.PWD_SHORT');
      return;
    }

    this.isLoading.set(true);

    this.authService.register({ name: this.name, email: this.email, password: this.password, role: 0 }).subscribe({
      next: () => {
        this.authService.login({ email: this.email, password: this.password }).subscribe({
          next: (resp) => {
            this.authService.saveSession(resp);
            this.isLoading.set(false);
            this.router.navigate(['/user/dashboard']);
          },
          error: () => {
            this.isLoading.set(false);
            this.router.navigate(['/login']);
          }
        });
      },
      error: (err: Error) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.message);
      }
    });
  }
}
