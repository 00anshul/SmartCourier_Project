import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (requiredRole: 'ADMIN' | 'CUSTOMER'): CanActivateFn => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    const role = authService.getRole() ?? '';
    const isAdmin = role.includes('ADMIN');
    const isCustomer = role.includes('CUSTOMER');

    if (requiredRole === 'ADMIN' && isAdmin) return true;
    if (requiredRole === 'CUSTOMER' && isCustomer) return true;

    // Wrong role — redirect to their correct dashboard
    if (isAdmin) router.navigate(['/admin/dashboard']);
    else if (isCustomer) router.navigate(['/customer/dashboard']);
    else router.navigate(['/login']);

    return false;
  };
};
