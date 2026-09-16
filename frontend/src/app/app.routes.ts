import { Routes } from '@angular/router';

import { DashboardComponent } from './components/dashboard/dashboard.component';
import { LayoutComponent } from './components/layout/layout.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { WallsComponent } from './components/walls/walls.component';
import { WallDetailComponent } from './components/wall-detail/wall-detail.component';
import { CatalogComponent } from './components/catalog/catalog.component';
import { CatalogDetailComponent } from './components/catalog-detail/catalog-detail.component';
import { ProfileComponent } from './components/profile/profile.component';
import { authGuard } from './guards/auth.guard';

// Defines public login/register routes and pages protected by the login guard.
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  // These pages share the layout; the guard requires a saved token, and the backend checks its
  // validity.
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'walls', component: WallsComponent },
      { path: 'walls/:id', component: WallDetailComponent },
      { path: 'catalog', component: CatalogComponent },
      { path: 'catalog/:type/:id', component: CatalogDetailComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'users/:username', component: ProfileComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
