import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/services/api.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirm = control.get('confirmPassword');
  if (password && confirm && password.value !== confirm.value) {
    confirm.setErrors({ passwordMismatch: true });
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="auth-page">
      <div class="auth-left">
        <div class="auth-brand">
          <mat-icon class="brand-icon">local_shipping</mat-icon>
          <h1>SmartCourier</h1>
          <p>Join thousands of happy customers</p>
        </div>
        <ul class="perks">
          <li><mat-icon>check_circle</mat-icon>Track all your parcels in one place</li>
          <li><mat-icon>check_circle</mat-icon>Book deliveries in minutes</li>
          <li><mat-icon>check_circle</mat-icon>Get real-time status updates</li>
          <li><mat-icon>check_circle</mat-icon>Multiple service options</li>
        </ul>
      </div>
      <div class="auth-right">
        <mat-card class="auth-card">
          <mat-card-header>
            <mat-card-title>Create account</mat-card-title>
            <mat-card-subtitle>Start shipping smarter today</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <form [formGroup]="form" (ngSubmit)="onSubmit()" id="register-form">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Full Name</mat-label>
                <input matInput formControlName="fullName" placeholder="Jane Doe" id="register-fullname">
                <mat-icon matSuffix>person</mat-icon>
                <mat-error *ngIf="form.get('fullName')?.hasError('required')">Full name is required</mat-error>
                <mat-error *ngIf="form.get('fullName')?.hasError('minlength')">At least 2 characters</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email address</mat-label>
                <input matInput formControlName="email" type="email" placeholder="you@example.com" id="register-email">
                <mat-icon matSuffix>mail</mat-icon>
                <mat-error *ngIf="form.get('email')?.hasError('required')">Email is required</mat-error>
                <mat-error *ngIf="form.get('email')?.hasError('email')">Enter a valid email</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Phone number</mat-label>
                <input matInput formControlName="phone" placeholder="+91 98765 43210" id="register-phone">
                <mat-icon matSuffix>phone</mat-icon>
                <mat-error *ngIf="form.get('phone')?.hasError('required')">Phone is required</mat-error>
                <mat-error *ngIf="form.get('phone')?.hasError('pattern')">Enter a valid phone number</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Password</mat-label>
                <input matInput formControlName="password" [type]="hidePass ? 'password' : 'text'" id="register-password">
                <button mat-icon-button matSuffix type="button" (click)="hidePass = !hidePass">
                  <mat-icon>{{ hidePass ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-error *ngIf="form.get('password')?.hasError('required')">Password is required</mat-error>
                <mat-error *ngIf="form.get('password')?.hasError('minlength')">Minimum 6 characters</mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Confirm Password</mat-label>
                <input matInput formControlName="confirmPassword" [type]="hideConfirm ? 'password' : 'text'" id="register-confirm-password">
                <button mat-icon-button matSuffix type="button" (click)="hideConfirm = !hideConfirm">
                  <mat-icon>{{ hideConfirm ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-error *ngIf="form.get('confirmPassword')?.hasError('required')">Please confirm your password</mat-error>
                <mat-error *ngIf="form.get('confirmPassword')?.hasError('passwordMismatch')">Passwords do not match</mat-error>
              </mat-form-field>

              <button mat-flat-button class="submit-btn" type="submit"
                      [disabled]="form.invalid || loading" id="register-submit-btn">
                <mat-icon *ngIf="!loading">person_add</mat-icon>
                <span>{{ loading ? 'Creating account...' : 'Create Account' }}</span>
              </button>
            </form>
          </mat-card-content>
          <mat-card-actions>
            <p class="auth-link">Already have an account? <a routerLink="/login" id="go-login-link">Sign in</a></p>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .auth-page { min-height: 100vh; display: flex; }
    .auth-left {
      flex: 1;
      background: linear-gradient(135deg, #1E3A5F 0%, #1D4ED8 100%);
      display: flex; flex-direction: column; justify-content: center; align-items: center;
      padding: 60px 48px; color: white;
    }
    .auth-brand { text-align: center; margin-bottom: 40px; }
    .auth-brand mat-icon { font-size: 64px; width: 64px; height: 64px; color: #93C5FD; margin-bottom: 16px; }
    .auth-brand h1 { font-size: 2.2rem; font-weight: 800; margin: 0 0 8px; }
    .auth-brand p { opacity: 0.8; }
    .perks { list-style: none; padding: 0; display: flex; flex-direction: column; gap: 14px; }
    .perks li { display: flex; align-items: center; gap: 10px; font-size: 0.95rem; opacity: 0.9; }
    .perks mat-icon { color: #86EFAC; font-size: 20px; }
    .auth-right {
      flex: 0 0 500px;
      display: flex; align-items: center; justify-content: center;
      background: #F8FAFC; padding: 40px;
    }
    .auth-card { width: 100%; max-width: 420px; border-radius: 16px !important; box-shadow: 0 4px 24px rgba(0,0,0,0.08) !important; }
    mat-card-header { padding: 24px 24px 0; }
    mat-card-title { font-size: 1.5rem !important; font-weight: 700 !important; color: #111827 !important; }
    mat-card-subtitle { color: #6B7280 !important; margin-top: 4px !important; }
    mat-card-content { padding: 20px 24px !important; }
    .full-width { width: 100%; margin-bottom: 4px; }
    .submit-btn {
      width: 100%; height: 48px; background: #1E3A5F; color: white;
      border-radius: 8px; font-size: 1rem; font-weight: 600; margin-top: 8px;
      display: flex; align-items: center; justify-content: center; gap: 8px; transition: background 0.2s;
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
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  hidePass = true;
  hideConfirm = true;
  loading = false;

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^[0-9+\-\s()]{7,15}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  }, { validators: passwordMatchValidator });

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const { confirmPassword, ...payload } = this.form.value;

    this.api.register(payload as any).subscribe({
      next: () => {
        this.snackBar.open('Account created! Please sign in.', '✓', { duration: 3000, panelClass: ['sc-snackbar-success'] });
        this.router.navigate(['/login']);
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => { this.loading = false; }
    });
  }
}
