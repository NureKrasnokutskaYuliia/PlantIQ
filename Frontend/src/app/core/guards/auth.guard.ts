import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const auth = inject(AuthService);

  if (!auth.isLoggedIn()) {
    return router.parseUrl('/login');
  }

  const url = state.url;

  if (auth.isAdmin() && (url.startsWith('/user') || url === '/')) {
    return router.parseUrl('/admin/dashboard');
  }

  if (!auth.isAdmin() && (url.startsWith('/admin') || url === '/')) {
    return router.parseUrl('/user/dashboard');
  }

  return true;
};

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const auth = inject(AuthService);

  if (!auth.isAdmin()) {
    return router.parseUrl('/user/dashboard');
  }
  return true;
};
