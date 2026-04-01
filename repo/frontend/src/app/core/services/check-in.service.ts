import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';
import { CheckInDetailModel, CheckInSummaryModel } from '../models/check-in.models';
import { PageResponse, PageParams } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CheckInService {
  private readonly silentContext = new HttpContext().set(SKIP_ERROR_TOAST, true);
  private readonly apiBaseUrl = environment.apiBaseUrl.endsWith('/') ? environment.apiBaseUrl.slice(0, -1) : environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  list(params?: PageParams): Observable<PageResponse<CheckInSummaryModel>> {
    let httpParams = new HttpParams();
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    return this.http.get<PageResponse<CheckInSummaryModel>>('/check-ins', { context: this.silentContext, withCredentials: true, params: httpParams });
  }

  get(id: number): Observable<CheckInDetailModel> {
    return this.http.get<CheckInDetailModel>(`/check-ins/${id}`, { context: this.silentContext, withCredentials: true });
  }

  create(payload: string, files: File[]): Observable<CheckInDetailModel> {
    const formData = new FormData();
    formData.append('payload', payload);
    files.forEach((file) => formData.append('files', file));
    return this.http.post<CheckInDetailModel>('/check-ins', formData, { withCredentials: true });
  }

  update(id: number, payload: string, files: File[]): Observable<CheckInDetailModel> {
    const formData = new FormData();
    formData.append('payload', payload);
    files.forEach((file) => formData.append('files', file));
    return this.http.put<CheckInDetailModel>(`/check-ins/${id}`, formData, { withCredentials: true });
  }

  attachmentUrl(checkInId: number, attachmentId: number): string {
    return `${this.apiBaseUrl}/check-ins/${checkInId}/attachments/${attachmentId}/download`;
  }
}
