import { Injectable } from '@angular/core';
import { HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';
import { CriticalActionRequestModel } from '../models/critical-action.models';
import { PageResponse, PageParams } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class CriticalActionService {
  private readonly silentContext = new HttpContext().set(SKIP_ERROR_TOAST, true);

  constructor(private readonly apiService: ApiService) {}

  list(params?: PageParams & { status?: string }): Observable<PageResponse<CriticalActionRequestModel>> {
    let httpParams = new HttpParams();
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    return this.apiService.get<PageResponse<CriticalActionRequestModel>>('/critical-actions', { context: this.silentContext, params: httpParams });
  }

  get(id: number): Observable<CriticalActionRequestModel> {
    return this.apiService.get<CriticalActionRequestModel>(`/critical-actions/${id}`, { context: this.silentContext });
  }

  create(body: unknown): Observable<CriticalActionRequestModel> {
    return this.apiService.post<CriticalActionRequestModel>('/critical-actions', body);
  }

  decide(id: number, body: unknown): Observable<CriticalActionRequestModel> {
    return this.apiService.post<CriticalActionRequestModel>(`/critical-actions/${id}/decision`, body);
  }
}
