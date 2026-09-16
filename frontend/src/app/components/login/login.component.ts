import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';

// Collects login details with a reactive form and navigates after successful login.
@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: '../auth-form.css'
})
export class LoginComponent {

  loginForm: FormGroup;
  errorMessage = '';
  loading = false;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // The interceptor adds this reason when the backend rejects a saved token.
    if (this.route.snapshot.queryParamMap.get('reason') === 'session-expired') {
      this.errorMessage = 'Your session expired. Please log in again.';
    }
  }

  // Show invalid fields before submitting; on success, the service saves the session.
  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const username = this.loginForm.value.username;
    const password = this.loginForm.value.password;

    this.authService.login(username, password).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: error => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Login failed';
      }
    });
  }
}
