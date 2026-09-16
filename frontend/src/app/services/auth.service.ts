import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthResponse, RegisterRequest } from '../models/auth.model';

// Sends login/registration requests with HttpClient and keeps the local session.
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = environment.apiUrl + '/auth';

  constructor(private http: HttpClient) {
  }

  // tap saves the returned session without changing the response received by the component.
  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl + '/register', request)
      .pipe(
        tap(response => this.saveSession(response))
      );
  }

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl + '/login', {
      username: username,
      password: password
    }).pipe(
      tap(response => this.saveSession(response))
    );
  }

  saveSession(response: AuthResponse): void {
    // Keep the JWT and username across reloads, but never store the password.
    localStorage.setItem('token', response.token);
    localStorage.setItem('username', response.username);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  isLoggedIn(): boolean {
    // Check only whether a token is saved; the backend checks if it is valid.
    return this.getToken() !== null;
  }

  // Remove the local token and username; this does not cancel a JWT already issued by the server.
  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
  }
}
