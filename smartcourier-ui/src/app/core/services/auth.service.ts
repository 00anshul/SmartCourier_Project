import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { AuthResponse, TokenPayload } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'sc_token';
  private readonly USER_KEY = 'sc_user';

  constructor(private router: Router) {}

  saveSession(auth: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, auth.accessToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      userId: auth.userId,
      fullName: auth.fullName,
      role: auth.role
    }));
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRole(): string | null {
    const user = this.getUser();
    return user?.role ?? null;
  }

  getUserId(): number | null {
    const user = this.getUser();
    return user?.userId ?? null;
  }

  getFullName(): string | null {
    const user = this.getUser();
    return user?.fullName ?? null;
  }

  private getUser(): { userId: number; fullName: string; role: string } | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const decoded = jwtDecode<TokenPayload>(token);
      return decoded.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  isAdmin(): boolean {
    const role = this.getRole();
    return role === 'ROLE_ADMIN' || role === 'ADMIN';
  }

  isCustomer(): boolean {
    const role = this.getRole();
    return role === 'ROLE_CUSTOMER' || role === 'CUSTOMER';
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.router.navigate(['/login']);
  }
}
