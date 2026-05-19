import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-book-parcel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatCardModule, MatFormFieldModule, MatInputModule,
            MatButtonModule, MatIconModule, MatSelectModule, MatStepperModule, MatDividerModule],
  template: `
    <div class="page-header">
      <div>
        <h2 class="page-title">Book a Parcel</h2>
        <p class="page-subtitle">Fill in the details to create a new delivery</p>
      </div>
    </div>

    <mat-card class="book-card">
      <mat-card-content>
        <mat-stepper [linear]="true" #stepper id="book-stepper">

          <!-- Step 1: Package Details -->
          <mat-step [stepControl]="packageForm" label="Package Details">
            <form [formGroup]="packageForm" id="package-form">
              <div class="step-content">
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Service Type</mat-label>
                    <mat-select formControlName="serviceType" id="serviceType">
                      <mat-option value="DOMESTIC">Domestic (3–5 days)</mat-option>
                      <mat-option value="EXPRESS">Express (1–2 days)</mat-option>
                      <mat-option value="INTERNATIONAL">International (7–14 days)</mat-option>
                    </mat-select>
                    <mat-error *ngIf="packageForm.get('serviceType')?.hasError('required')">Service type is required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Weight (kg)</mat-label>
                    <input matInput formControlName="weightKg" type="number" step="0.1" min="0.1" placeholder="e.g. 2.5" id="weightKg">
                    <mat-icon matSuffix>scale</mat-icon>
                    <mat-error *ngIf="packageForm.get('weightKg')?.hasError('required')">Weight is required</mat-error>
                    <mat-error *ngIf="packageForm.get('weightKg')?.hasError('min')">Weight must be > 0</mat-error>
                  </mat-form-field>
                </div>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="third-width">
                    <mat-label>Length (cm)</mat-label>
                    <input matInput formControlName="lengthCm" type="number" placeholder="30" id="lengthCm">
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="third-width">
                    <mat-label>Width (cm)</mat-label>
                    <input matInput formControlName="widthCm" type="number" placeholder="20" id="widthCm">
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="third-width">
                    <mat-label>Height (cm)</mat-label>
                    <input matInput formControlName="heightCm" type="number" placeholder="15" id="heightCm">
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Description (optional)</mat-label>
                  <textarea matInput formControlName="description" placeholder="What are you sending?" rows="2" id="description"></textarea>
                </mat-form-field>

                <div class="quote-box" *ngIf="quote !== null">
                  <mat-icon>receipt_long</mat-icon>
                  <span>Estimated charge: <strong>₹{{ quote | number:'1.2-2' }}</strong></span>
                </div>

                <div class="step-actions">
                  <button mat-button type="button" (click)="fetchQuote()" id="get-quote-btn" [disabled]="!packageForm.get('serviceType')?.valid || !packageForm.get('weightKg')?.valid">
                    <mat-icon>calculate</mat-icon> Get Quote
                  </button>
                  <button mat-flat-button matStepperNext class="primary-btn" type="button" [disabled]="packageForm.invalid" id="step1-next">Next</button>
                </div>
              </div>
            </form>
          </mat-step>

          <!-- Step 2: Sender Address -->
          <mat-step [stepControl]="senderForm" label="Sender Info">
            <form [formGroup]="senderForm" id="sender-form">
              <div class="step-content">
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Contact Name</mat-label>
                    <input matInput formControlName="fullName" placeholder="John Doe" id="sender-contact-name">
                    <mat-error *ngIf="senderForm.get('fullName')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Contact Phone</mat-label>
                    <input matInput formControlName="phone" placeholder="+91 98765 43210" id="sender-phone">
                    <mat-error *ngIf="senderForm.get('phone')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Street Address</mat-label>
                  <input matInput formControlName="street" placeholder="123 Main St" id="sender-street">
                  <mat-error *ngIf="senderForm.get('street')?.hasError('required')">Required</mat-error>
                </mat-form-field>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>City</mat-label>
                    <input matInput formControlName="city" id="sender-city">
                    <mat-error *ngIf="senderForm.get('city')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>State</mat-label>
                    <input matInput formControlName="state" id="sender-state">
                    <mat-error *ngIf="senderForm.get('state')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>PIN Code</mat-label>
                    <input matInput formControlName="pincode" id="sender-zip">
                    <mat-error *ngIf="senderForm.get('pincode')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Country</mat-label>
                    <input matInput formControlName="country" id="sender-country">
                    <mat-error *ngIf="senderForm.get('country')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <div class="step-actions">
                  <button mat-button matStepperPrevious type="button">Back</button>
                  <button mat-flat-button matStepperNext class="primary-btn" type="button" [disabled]="senderForm.invalid" id="step2-next">Next</button>
                </div>
              </div>
            </form>
          </mat-step>

          <!-- Step 3: Receiver Address -->
          <mat-step [stepControl]="receiverForm" label="Receiver Info">
            <form [formGroup]="receiverForm" id="receiver-form">
              <div class="step-content">
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Contact Name</mat-label>
                    <input matInput formControlName="fullName" placeholder="Jane Doe" id="receiver-contact-name">
                    <mat-error *ngIf="receiverForm.get('fullName')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Contact Phone</mat-label>
                    <input matInput formControlName="phone" placeholder="+91 98765 43210" id="receiver-phone">
                    <mat-error *ngIf="receiverForm.get('phone')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Street Address</mat-label>
                  <input matInput formControlName="street" id="receiver-street">
                  <mat-error *ngIf="receiverForm.get('street')?.hasError('required')">Required</mat-error>
                </mat-form-field>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>City</mat-label>
                    <input matInput formControlName="city" id="receiver-city">
                    <mat-error *ngIf="receiverForm.get('city')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>State</mat-label>
                    <input matInput formControlName="state" id="receiver-state">
                    <mat-error *ngIf="receiverForm.get('state')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <div class="form-row">
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>PIN Code</mat-label>
                    <input matInput formControlName="pincode" id="receiver-zip">
                    <mat-error *ngIf="receiverForm.get('pincode')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                  <mat-form-field appearance="outline" class="half-width">
                    <mat-label>Country</mat-label>
                    <input matInput formControlName="country" id="receiver-country">
                    <mat-error *ngIf="receiverForm.get('country')?.hasError('required')">Required</mat-error>
                  </mat-form-field>
                </div>
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Notes (optional)</mat-label>
                  <textarea matInput formControlName="notes" placeholder="Any special instructions?" rows="2" id="delivery-notes"></textarea>
                </mat-form-field>
                <div class="step-actions">
                  <button mat-button matStepperPrevious type="button">Back</button>
                  <button mat-flat-button class="primary-btn" type="button" (click)="onSubmit()" [disabled]="receiverForm.invalid || loading" id="submit-booking-btn">
                    <mat-icon>send</mat-icon>
                    {{ loading ? 'Booking...' : 'Confirm Booking' }}
                  </button>
                </div>
              </div>
            </form>
          </mat-step>

        </mat-stepper>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .page-header { margin-bottom: 24px; }
    .page-title { font-size: 1.8rem; font-weight: 700; color: #111827; margin: 0 0 4px; }
    .page-subtitle { color: #6B7280; margin: 0; }
    .book-card { border-radius: 16px !important; box-shadow: 0 4px 16px rgba(0,0,0,0.08) !important; }
    mat-card-content { padding: 24px !important; }
    .step-content { padding: 20px 0; }
    .full-width { width: 100%; margin-bottom: 8px; }
    .half-width { flex: 1; min-width: 0; }
    .third-width { flex: 1; min-width: 0; }
    .form-row { display: flex; gap: 12px; margin-bottom: 0; }
    .step-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }
    .primary-btn { background: #1E3A5F; color: white; border-radius: 8px; }
    .quote-box {
      display: flex; align-items: center; gap: 8px;
      background: #EFF6FF; border: 1px solid #BFDBFE;
      border-radius: 8px; padding: 12px 16px; margin: 8px 0;
      color: #1D4ED8; font-size: 0.9rem;
    }
  `]
})
export class BookParcelComponent {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);
  private cdr = inject(ChangeDetectorRef);

  loading = false;
  quote: number | null = null;

  packageForm = this.fb.group({
    serviceType: ['', Validators.required],
    weightKg: [null as number | null, [Validators.required, Validators.min(0.1)]],
    lengthCm: [null as number | null],
    widthCm: [null as number | null],
    heightCm: [null as number | null],
    description: ['']
  });

  private addressGroup() {
    return this.fb.group({
      fullName: ['', Validators.required],
      phone: ['', Validators.required],
      street: ['', Validators.required],
      city: ['', Validators.required],
      state: ['', Validators.required],
      pincode: ['', Validators.required],
      country: ['India', Validators.required],
    });
  }

  senderForm = this.addressGroup();

  // notes must be inside fb.group() so Angular reactive forms can find it via formControlName
  receiverForm = this.fb.group({
    fullName: ['', Validators.required],
    phone: ['', Validators.required],
    street: ['', Validators.required],
    city: ['', Validators.required],
    state: ['', Validators.required],
    pincode: ['', Validators.required],
    country: ['India', Validators.required],
    notes: ['']
  });

  fetchQuote(): void {
    const { serviceType, weightKg } = this.packageForm.value;
    if (!serviceType || !weightKg) return;
    this.api.getQuote(serviceType, weightKg).subscribe({
      next: (res) => {
        this.quote = res.data;
        this.cdr.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.packageForm.invalid || this.senderForm.invalid || this.receiverForm.invalid) return;
    this.loading = true;

    const { notes, ...receiverAddr } = this.receiverForm.value;
    const payload = {
      serviceType: this.packageForm.value.serviceType!,
      notes: notes ?? '',
      packageDetails: {
        weightKg: this.packageForm.value.weightKg!,
        lengthCm: this.packageForm.value.lengthCm ?? undefined,
        widthCm: this.packageForm.value.widthCm ?? undefined,
        heightCm: this.packageForm.value.heightCm ?? undefined,
        description: this.packageForm.value.description ?? ''
      },
      senderAddress: this.senderForm.value as any,
      receiverAddress: receiverAddr as any
    };

    this.api.createDelivery(payload as any).subscribe({
      next: (res) => {
        this.snackBar.open(`Parcel booked! Tracking: ${res.data.trackingNumber}`, '✓', { duration: 5000, panelClass: ['sc-snackbar-success'] });
        this.router.navigate(['/customer/history']);
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
      complete: () => { this.loading = false; }
    });
  }
}
