import { Injectable } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { MatIconRegistry } from '@angular/material/icon';

@Injectable({ providedIn: 'root' })
export class IconService {
  private registered = false;

  constructor(
    private readonly iconRegistry: MatIconRegistry,
    private readonly sanitizer: DomSanitizer
  ) {}

  registerIcons(): void {
    if (this.registered) {
      return;
    }

    const icons = [
      'dashboard',
      'orders',
      'review',
      'payments',
      'fulfillment',
      'receipts',
      'returns',
      'documents',
      'checkins',
      'approvals',
      'admin',
      'placeholder',
      'eye',
      'eye-off',
      'menu'
    ];

    icons.forEach((icon) => {
      this.iconRegistry.addSvgIcon(icon, this.sanitizer.bypassSecurityTrustResourceUrl(`assets/icons/${icon}.svg`));
    });

    this.registered = true;
  }
}
