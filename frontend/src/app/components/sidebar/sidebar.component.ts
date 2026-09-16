import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../services/auth.service';

// Displays navigation and lets the user log out.
@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {

  // The parent controls collapse state; @Output asks it to change that state.
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

  // Clear the local session, then return to the login page.
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
