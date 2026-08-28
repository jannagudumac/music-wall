import { Component, HostListener } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';

import { SidebarComponent } from '../sidebar/sidebar.component';
import { PageHeaderService } from '../../services/page-header.service';

@Component({
  selector: 'app-layout',
  imports: [RouterLink, RouterOutlet, SidebarComponent],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

  sidebarCollapsed = false;
  showBackToTop = false;

  constructor(
    private router: Router,
    private pageHeaderService: PageHeaderService
  ) {
  }

  get pageTitle(): string {
    const detailTitle = this.pageHeaderService.detailTitle();
    if (detailTitle) {
      return detailTitle;
    }

    const segment = this.router.url.split('?')[0].split('/').filter(Boolean)[0] || 'dashboard';
    const titles: Record<string, string> = {
      dashboard: 'Dashboard',
      walls: 'My walls',
      catalog: 'Catalogue',
      profile: 'Profile',
      users: 'Profile'
    };
    return titles[segment] || 'Music Wall';
  }

  get wallDetailTitle(): string | null {
    return this.pageHeaderService.detailTitle();
  }

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.showBackToTop = window.scrollY > 500;
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

}
