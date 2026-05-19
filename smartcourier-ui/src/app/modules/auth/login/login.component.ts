import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="auth-page">
      <div class="auth-left">
        <div class="auth-brand">
          <mat-icon class="brand-icon">local_shipping</mat-icon>
          <h1>SmartCourier</h1>
          <p>Intelligent logistics at your fingertips</p>
        </div>
        <div class="auth-features">
          <div class="feature"><mat-icon>radar</mat-icon><span>Real-time parcel tracking</span></div>
          <div class="feature"><mat-icon>speed</mat-icon><span>Fast & reliable delivery</span></div>
          <div class="feature"><mat-icon>security</mat-icon><span>Secure & transparent</span></div>
        </div>
      </div>

      <div class="auth-right">
        <mat-card class="auth-card">
          <mat-card-header>
            <mat-card-title>Welcome back</mat-card-title>
            <mat-card-subtitle>Sign in to your account</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <form [formGroup]="form" (ngSubmit)="onSubmit()" id="login-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email address</mat-label>
                <input matInput formControlName="email" type="email" placeholder="you@example.com" id="login-email">
                <mat-icon matSuffix>mail</mat-icon>
                <mat-error *ngIf="form.get('email')?.hasError('required')">Email is required</mat-error>
                <mat-error *ngIf="form.get('email')?.hasError('email')">Enter a valid email</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Password</mat-label>
                <input matInput formControlName="password" [type]="hidePassword ? 'password' : 'text'" id="login-password">
                <button mat-icon-button matSuffix type="button" (click)="hidePassword = !hidePassword">
                  <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-error *ngIf="form.get('password')?.hasError('required')">Password is required</mat-error>
                <mat-error *ngIf="form.get('password')?.hasError('minlength')">Minimum 6 characters</mat-error>
              </mat-form-field>

              <button mat-flat-button class="submit-btn" type="submit"
                      [disabled]="form.invalid || loading" id="login-submit-btn">
                <mat-icon *ngIf="!loading">login</mat-icon>
                <span>{{ loading ? 'Signing in...' : 'Sign In' }}</span>
              </button>
            </form>
          </mat-card-content>
          <mat-card-actions>
            <p class="auth-link">New to SmartCourier? <a routerLink="/register" id="go-register-link">Create account</a></p>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      display: flex;
    }
    .auth-left {
      flex: 1;
      background: linear-gradient(135deg, #1E3A5F 0%, #2563EB 100%);
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 60px 48px;
      color: white;
    }
    .auth-brand { text-align: center; margin-bottom: 48px; }
    .auth-brand mat-icon { font-size: 64px; width: 64px; height: 64px; color: #93C5FD; margin-bottom: 16px; }
    .auth-brand h1 { font-size: 2.5rem; font-weight: 800; margin: 0 0 8px; }
    .auth-brand p { opacity: 0.8; font-size: 1rem; }
    .auth-features { display: flex; flex-direction: column; gap: 16px; }
    .feature { display: flex; align-items: center; gap: 12px; font-size: 1rem; opacity: 0.9; }
    .feature mat-icon { color: #93C5FD; }
    .auth-right {
      flex: 0 0 480px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F8FAFC;
      padding: 40px;
    }
    .auth-card {
      width: 100%;
      max-width: 400px;
      border-radius: 16px !important;
      box-shadow: 0 4px 24px rgba(0,0,0,0.08) !important;
    }
    mat-card-header { padding: 24px 24px 0; }
    mat-card-title { font-size: 1.5rem !important; font-weight: 700 !important; color: #111827 !important; }
    mat-card-subtitle { color: #6B7280 !important; margin-top: 4px !important; }
    mat-card-content { padding: 20px 24px !important; }
    .full-width { width: 100%; margin-bottom: 4px; }
    .submit-btn {
      width: 100%;
      height: 48px;
      background: #1E3A5F;
      color: white;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      margin-top: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: background 0.2s;
    }
    .submit-btn:hover { background: #2563EB; }
    mat-card-actions { padding: 8px 24px 24px; }
    .auth-link { color: #6B7280; font-size: 0.875rem; text-align: center; margin: 0; }
    .auth-link a { color: #2563EB; font-weight: 600; text-decoration: none; }
    @media (max-width: 768px) {
      .auth-page { flex-direction: column; }
      .auth-left { padding: 40px 24px; }
      .auth-right { flex: 1; padding: 20px 16px; }
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  hidePassword = true;
  loading = false;

  form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;

    this.api.login(this.form.value as any).subscribe({
      next: (res) => {
        this.authService.saveSession(res.data);
        this.snackBar.open(`Welcome back, ${res.data.fullName}!`, '✓', { duration: 3000, panelClass: ['sc-snackbar-success'] });
        if (this.authService.isAdmin()) this.router.navigate(['/admin/dashboard']);
        else this.router.navigate(['/customer/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
