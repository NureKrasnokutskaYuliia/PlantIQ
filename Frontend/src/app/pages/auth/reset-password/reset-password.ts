import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { LanguageSwitcher, ThemeToggle } from '../../../shared/components/index';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [RouterLink, FormsModule, TranslateModule, LanguageSwitcher, ThemeToggle],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss'
})
export class ResetPassword implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  email = '';
  code = '';
  newPassword = '';
  confirmPassword = '';
  isLoading = signal(false);
  errorMessage = signal('');
  success = signal(false);

  ngOnInit() {
    this.email = this.route.snapshot.queryParamMap.get('email') ?? '';
  }

  get passwordMismatch(): boolean {
    return this.confirmPassword.length > 0 && this.newPassword !== this.confirmPassword;
  }

  get passwordTooShort(): boolean {
    return this.newPassword.length > 0 && this.newPassword.length < 6;
  }

  get codeIncomplete(): boolean {
    return this.code.length > 0 && this.code.length < 6;
  }

  get isFormValid(): boolean {
    return this.code.length === 6
      && this.newPassword.length >= 6
      && this.newPassword === this.confirmPassword;
  }

  submit() {
    this.errorMessage.set('');

    if (!this.code || !this.newPassword || !this.confirmPassword) {
      this.errorMessage.set('COMMON.ERRORS.REQUIRED_FIELDS');
      return;
    }
    if (this.newPassword.length < 6) {
      this.errorMessage.set('COMMON.ERRORS.PWD_SHORT');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage.set('AUTH.RESET.PWD_MISMATCH');
      return;
    }

    this.isLoading.set(true);

    this.authService.resetPassword(this.email, this.code, this.newPassword).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.success.set(true);
      },
      error: (err: Error) => {
        this.errorMessage.set(err.message);
        this.isLoading.set(false);
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}
