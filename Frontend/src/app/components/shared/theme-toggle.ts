import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button class="theme-btn" (click)="theme.toggle()" [title]="theme.darkMode() ? 'Switch to Light' : 'Switch to Dark'">
      {{ theme.darkMode() ? '☀️' : '🌙' }}
    </button>
  `,
  styles: [`
    .theme-btn {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      color: var(--text-primary);
      border-radius: 50%;
      width: 38px;
      height: 38px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 1.2rem;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);

      &:hover {
        border-color: var(--primary-color);
        background: var(--bg-secondary);
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
      }

      &:active {
        transform: translateY(0);
      }
    }
  `]
})
export class ThemeToggle {
  theme = inject(ThemeService);
}
