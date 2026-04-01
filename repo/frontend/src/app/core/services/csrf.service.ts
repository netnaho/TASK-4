import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private token = '';

  setToken(token: string): void {
    this.token = token;
  }

  getToken(): string {
    return this.token;
  }
}
