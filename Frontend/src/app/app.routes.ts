import { Routes } from '@angular/router';
import { AdminLayout } from './shared/layout/admin-layout/admin-layout';
import { UserLayout } from './shared/layout/user-layout/user-layout';

import { AdminDashboard } from './pages/admin-dashboard/admin-dashboard';
import { AdminUsers } from './pages/admin-users/admin-users';
import { AdminData } from './pages/admin-data/admin-data';
import { AdminNotifications } from './pages/admin-notifications/admin-notifications';
import { AdminSettings } from './pages/admin-settings/admin-settings';
import { AdminExport } from './pages/admin-export/admin-export';

import { UserDashboard } from './pages/user-dashboard/user-dashboard';
import { PlantsList } from './pages/plants-list/plants-list';
import { MyDevices } from './pages/my-devices/my-devices';
import { Watering } from './pages/watering/watering';
import { Analytics } from './pages/analytics/analytics';
import { Profile } from './pages/profile/profile';
import { Login } from './pages/auth/login/login';
import { Register } from './pages/auth/register/register';
import { authGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'dashboard', component: AdminDashboard },
      { path: 'users', component: AdminUsers },
      { path: 'data', component: AdminData },
      { path: 'notifications', component: AdminNotifications },
      { path: 'settings', component: AdminSettings },
      { path: 'export', component: AdminExport },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  {
    path: 'user',
    component: UserLayout,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: UserDashboard },
      { path: 'plants', component: PlantsList },
      { path: 'devices', component: MyDevices },
      { path: 'watering', component: Watering },
      { path: 'analytics', component: Analytics },
      { path: 'profile', component: Profile },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
