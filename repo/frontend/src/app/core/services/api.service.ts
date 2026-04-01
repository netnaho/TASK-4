import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  get<T>(path: string, options?: { context?: HttpContext; params?: HttpParams }): Observable<T> {
    return this.http.get<T>(path, options);
  }

  post<T>(path: string, body: unknown, options?: { context?: HttpContext }): Observable<T> {
    return this.http.post<T>(path, body, options);
  }

  put<T>(path: string, body: unknown, options?: { context?: HttpContext }): Observable<T> {
    return this.http.put<T>(path, body, options);
  }
}
