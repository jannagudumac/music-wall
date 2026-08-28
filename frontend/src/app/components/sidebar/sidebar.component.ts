import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {

  @Input() collapsed = false;
  @Output() collapseRequested = new EventEmitter<void>();

  constructor(
    public authService: AuthService,
    private router: Router
  ) {
  }

  get usernameInitial(): string {
    return (this.authService.getUsername() || 'U').charAt(0).toUpperCase();
  }

  toggleCollapse(): void {
    this.collapseRequested.emit();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
