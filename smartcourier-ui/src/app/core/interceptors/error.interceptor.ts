import { HttpInterceptorFn, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, tap, throwError } from 'rxjs';
import { LoadingService } from '../services/loading.service';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const loadingService = inject(LoadingService);
  const authService = inject(AuthService);

  const TOKEN_INVALIDATED_MSGS = [
    'token has been invalidated',
    'please login again',
    'invalid token',
    'token expired',
  ];

  return next(req).pipe(
    // Catch 200-level responses where the backend signals auth failure in the body
    tap((event) => {
      if (event instanceof HttpResponse) {
        const body = event.body as any;
        if (body && body.success === false && body.message) {
          const msg: string = body.message.toLowerCase();
          const isAuthFailure = TOKEN_INVALIDATED_MSGS.some(m => msg.includes(m));
          if (isAuthFailure) {
            authService.logout(); // clears localStorage + navigates to /login
            snackBar.open('Session expired. Please log in again.', '✕', {
              duration: 4000,
              panelClass: ['sc-snackbar-error']
            });
          }
        }
      }
    }),
    catchError((error: HttpErrorResponse) => {
      queueMicrotask(() => loadingService.hide());
      let message = 'An unexpected error occurred';

      if (error.error?.message) {
        message = error.error.message;
      } else if (error.status === 0) {
        message = 'Cannot connect to server. Please check if the backend is running.';
      } else if (error.status === 401) {
        message = 'Session expired. Please log in again.';
        authService.logout();
      } else if (error.status === 403) {
        message = 'You do not have permission to perform this action.';
      } else if (error.status === 404) {
        message = 'Resource not found.';
      } else if (error.status === 409) {
        message = error.error?.message ?? 'A conflict occurred (e.g., email already in use).';
      } else if (error.status >= 500) {
        message = 'Server error. Please try again later.';
      }

      snackBar.open(message, '✕', {
        duration: 4000,
        panelClass: ['sc-snackbar-error']
      });

      return throwError(() => error);
    })
  );
};
