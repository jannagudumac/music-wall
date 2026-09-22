import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

// Adds the saved JWT to requests and handles rejected sessions.
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
  }

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    if (token) {
      // Clone the request because HttpRequest cannot be changed directly.
      request = request.clone({
        setHeaders: {
          Authorization: 'Bearer ' + token
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Log out on HTTP 401, but keep the session on 403 because 
        // that means a permission was denied.
        const sessionRejected = error.status === 401;
        const authenticationRequest = request.url.includes('/auth/');

        if (sessionRejected && !authenticationRequest) {
          this.authService.logout();
          this.router.navigate(['/login'], {
            queryParams: { reason: 'session-expired' }
          });
        }

        return throwError(() => error);
      })
    );
  }
}
