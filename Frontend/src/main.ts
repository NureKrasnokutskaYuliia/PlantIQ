import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

// Apply saved theme before Angular bootstraps to avoid flash of wrong theme
if (localStorage.getItem('theme') === 'dark') {
  document.documentElement.classList.add('dark-theme');
}

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
