import { Component, inject } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [TranslateModule],
  template: `
    <div class="lang-switcher">
      <label for="lang-select">{{ 'COMMON.LANGUAGE' | translate }}</label>
      <select id="lang-select" (change)="switchLanguage($event)">
        <option value="uk" [selected]="currentLang === 'uk'">{{ 'COMMON.UKRAINIAN' | translate }}</option>
        <option value="en" [selected]="currentLang === 'en'">{{ 'COMMON.ENGLISH' | translate }}</option>
      </select>
    </div>
  `,
  styles: [`
    .lang-switcher {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      color: inherit;
    }
    select {
      padding: 0.5rem 2rem 0.5rem 1rem;
      border-radius: 9999px;
      border: 1px solid var(--border-color);
      background: var(--bg-secondary);
      color: var(--text-primary);
      font-weight: 500;
      cursor: pointer;
      outline: none;
      transition: all 0.2s ease;
      appearance: none;
      background-image: url("data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%231e293b%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E");
      background-repeat: no-repeat;
      background-position: right 0.75rem top 50%;
      background-size: 0.65rem auto;
    }
    select:hover {
      background-color: var(--bg-primary);
      border-color: var(--primary-color);
    }
    select option {
      background: var(--card-bg);
      color: var(--text-primary);
    }
  `]
})
export class LanguageSwitcher {
  private translate = inject(TranslateService);
  currentLang = 'uk';

  constructor() {
    this.translate.addLangs(['uk', 'en']);
    const savedLang = localStorage.getItem('appLang');
    this.currentLang = savedLang || this.translate.currentLang || this.translate.getDefaultLang() || 'uk';
    this.setLanguage(this.currentLang);
  }

  switchLanguage(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.setLanguage(select.value);
  }

  private setLanguage(lang: string) {
    this.currentLang = lang;
    this.translate.use(lang);
    localStorage.setItem('appLang', lang);
    document.documentElement.lang = lang;
    document.documentElement.dir = lang === 'ar' || lang === 'he' ? 'rtl' : 'ltr';
  }
}
