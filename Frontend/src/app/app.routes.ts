import { Routes } from '@angular/router';
import { AdminLayout } from './components/layout/admin-layout/admin-layout';
import { UserLayout } from './components/layout/user-layout/user-layout';

import { AdminDashboard } from './components/pages/admin-dashboard/admin-dashboard';
import { AdminUsers } from './components/pages/admin-users/admin-users';
import { AdminData } from './components/pages/admin-data/admin-data';
import { AdminNotifications } from './components/pages/admin-notifications/admin-notifications';
import { AdminSettings } from './components/pages/admin-settings/admin-settings';
import { AdminExport } from './components/pages/admin-export/admin-export';

import { UserDashboard } from './components/pages/user-dashboard/user-dashboard';
import { PlantsList } from './components/pages/plants-list/plants-list';
import { MyDevices } from './components/pages/my-devices/my-devices';
import { Watering } from './components/pages/watering/watering';
import { Analytics } from './components/pages/analytics/analytics';
import { Profile } from './components/pages/profile/profile';
import { Login } from './components/pages/auth/login/login';
import { Register } from './components/pages/auth/register/register';
import { authGuard, adminGuard } from './guards/auth.guard';

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
