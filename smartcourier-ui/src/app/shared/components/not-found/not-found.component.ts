import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule],
  template: `
    <div class="not-found-page">
      <div class="content">
        <div class="error-code">404</div>
        <div class="error-icon"><mat-icon>search_off</mat-icon></div>
        <h1>Page Not Found</h1>
        <p>The page you're looking for doesn't exist or has been moved.</p>
        <a mat-flat-button routerLink="/login" class="home-btn" id="go-home-btn">
          <mat-icon>home</mat-icon> Go to Login
        </a>
      </div>
    </div>
  `,
  styles: [`
    .not-found-page {
      min-height: 100vh;
      display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #F8FAFC 0%, #EFF6FF 100%);
    }
    .content { text-align: center; }
    .error-code { font-size: 8rem; font-weight: 900; color: #1E3A5F; opacity: 0.15; line-height: 1; }
    .error-icon mat-icon { font-size: 64px; width: 64px; height: 64px; color: #1E3A5F; margin-top: -40px; }
    h1 { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 16px 0 8px; }
    p { color: #6B7280; margin: 0 0 28px; }
    .home-btn { background: #1E3A5F; color: white; border-radius: 8px; display: inline-flex; align-items: center; gap: 6px; }
  `]
})
export class NotFoundComponent {}
