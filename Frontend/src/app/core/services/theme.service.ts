import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  darkMode = signal<boolean>(localStorage.getItem('theme') === 'dark');

  constructor() {
    // Reapply class when service is instantiated (defensive fallback)
    document.documentElement.classList.toggle('dark-theme', this.darkMode());
  }

  toggle() {
    const isDark = !this.darkMode();
    this.darkMode.set(isDark);
    document.documentElement.classList.toggle('dark-theme', isDark);
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  }
}
