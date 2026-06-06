import { Component, inject, signal } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageSwitcher, ThemeToggle } from '../../../shared/components/index';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LanguageSwitcher, ThemeToggle],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss'
})
export class ForgotPassword {
  private router = inject(Router);
  private authService = inject(AuthService);

  email = '';
  isLoading = signal(false);
  errorMessage = signal('');
  successEmail = signal('');

  submit() {
    this.errorMessage.set('');

    if (!this.email) {
      this.errorMessage.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    if (!this.email.includes('@')) {
      this.errorMessage.set('COMMON.ERRORS.INVALID_EMAIL');
      return;
    }

    this.isLoading.set(true);

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/reset-password'], { queryParams: { email: this.email } });
      },
      error: (err: Error) => {
        this.errorMessage.set(err.message);
        this.isLoading.set(false);
      }
    });
  }
}
