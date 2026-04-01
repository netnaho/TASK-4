import { Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  DocumentArchiveModel,
  DocumentDetailModel,
  DocumentPreviewModel,
  DocumentSummaryModel,
  DocumentTemplateModel,
  DocumentTypeModel
} from '../models/document.models';
import { PageResponse, PageParams } from '../models/page.model';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DocumentCenterService {
  private readonly silentContext = new HttpContext().set(SKIP_ERROR_TOAST, true);
  private readonly apiBaseUrl = environment.apiBaseUrl.endsWith('/') ? environment.apiBaseUrl.slice(0, -1) : environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  listTypes(): Observable<DocumentTypeModel[]> {
    return this.http.get<DocumentTypeModel[]>('/documents/types', { context: this.silentContext, withCredentials: true });
  }

  listTemplates(): Observable<DocumentTemplateModel[]> {
    return this.http.get<DocumentTemplateModel[]>('/documents/templates', { context: this.silentContext, withCredentials: true });
  }

  createTemplate(payload: unknown): Observable<DocumentTemplateModel> {
    return this.http.post<DocumentTemplateModel>('/documents/templates', payload, { withCredentials: true });
  }

  listDocuments(params?: PageParams & { status?: string; documentTypeId?: number }): Observable<PageResponse<DocumentSummaryModel>> {
    let httpParams = new HttpParams();
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.documentTypeId != null) httpParams = httpParams.set('documentTypeId', params.documentTypeId);
    return this.http.get<PageResponse<DocumentSummaryModel>>('/documents', { context: this.silentContext, withCredentials: true, params: httpParams });
  }

  approvalQueue(params?: PageParams): Observable<PageResponse<DocumentSummaryModel>> {
    let httpParams = new HttpParams();
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    return this.http.get<PageResponse<DocumentSummaryModel>>('/documents/approval-queue', { context: this.silentContext, withCredentials: true, params: httpParams });
  }

  archiveList(): Observable<DocumentArchiveModel[]> {
    return this.http.get<DocumentArchiveModel[]>('/documents/archive', { context: this.silentContext, withCredentials: true });
  }

  getDocument(id: number): Observable<DocumentDetailModel> {
    return this.http.get<DocumentDetailModel>(`/documents/${id}`, { context: this.silentContext, withCredentials: true });
  }

  createDraft(payload: string, file?: File | null): Observable<DocumentDetailModel> {
    const formData = new FormData();
    formData.append('payload', payload);
    if (file) {
      formData.append('file', file);
    }
    return this.http.post<DocumentDetailModel>('/documents', formData, { withCredentials: true });
  }

  updateDraft(id: number, payload: string, file?: File | null): Observable<DocumentDetailModel> {
    const formData = new FormData();
    formData.append('payload', payload);
    if (file) {
      formData.append('file', file);
    }
    return this.http.put<DocumentDetailModel>(`/documents/${id}`, formData, { withCredentials: true });
  }

  submitForApproval(id: number): Observable<DocumentDetailModel> {
    return this.http.post<DocumentDetailModel>(`/documents/${id}/submit-approval`, {}, { withCredentials: true });
  }

  approve(id: number, comments: string): Observable<DocumentDetailModel> {
    return this.http.post<DocumentDetailModel>(`/documents/${id}/approve`, { comments }, { withCredentials: true });
  }

  archive(id: number): Observable<DocumentDetailModel> {
    return this.http.post<DocumentDetailModel>(`/documents/${id}/archive`, {}, { withCredentials: true });
  }

  preview(id: number): Observable<DocumentPreviewModel> {
    return this.http.get<DocumentPreviewModel>(`/documents/${id}/preview`, { withCredentials: true });
  }

  fetchContentBlob(id: number): Observable<Blob> {
    const url = new URL(this.contentUrl(id), window.location.origin).toString();
    return this.http.get(url, { responseType: 'blob', withCredentials: true });
  }

  contentUrl(id: number): string {
    return `${this.apiBaseUrl}/documents/${id}/content`;
  }

  downloadUrl(id: number): string {
    return `${this.apiBaseUrl}/documents/${id}/download`;
  }
}
