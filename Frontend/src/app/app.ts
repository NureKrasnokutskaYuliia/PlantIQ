import { Component, signal, inject, OnInit } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from './core/services/auth.service';
import { API_BASE_URL } from './core/services/api.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected readonly title = signal('frontend');

  public auth = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);

  ngOnInit() {
    if (!this.auth.isLoggedIn()) return;

    const userId = sessionStorage.getItem('userId');
    const token = this.auth.getToken();
    if (!userId || !token) return;

    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    this.http.get<{ role: 'Owner' | 'Admin'; isActive: boolean }>(`${API_BASE_URL}/Users/${userId}`, { headers })
      .subscribe({
        next: (user) => {
          if (!user.isActive) {
            this.auth.logout();
            this.router.navigate(['/login']);
            return;
          }
          const actualRole = user.role === 'Admin' ? 'admin' : 'user';
          const storedRole = sessionStorage.getItem('role');
          if (storedRole === actualRole) return;

          sessionStorage.setItem('role', actualRole);
          const currentUrl = this.router.url;
          if (actualRole === 'user' && currentUrl.startsWith('/admin')) {
            this.router.navigate(['/user/dashboard']);
          } else if (actualRole === 'admin' && currentUrl.startsWith('/user')) {
            this.router.navigate(['/admin/dashboard']);
          }
        },
        error: () => {}
      });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
