import { Injectable } from '@angular/core';
import { HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';
import { ReasonCodeModel, AdminUserModel, PermissionOverviewModel, StateMachineConfigModel } from '../models/admin.models';
import { DocumentTypeModel } from '../models/document.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly silentContext = new HttpContext().set(SKIP_ERROR_TOAST, true);

  constructor(private readonly apiService: ApiService) {}

  users(): Observable<AdminUserModel[]> {
    return this.apiService.get<AdminUserModel[]>('/admin/users', { context: this.silentContext });
  }

  permissions(): Observable<PermissionOverviewModel[]> {
    return this.apiService.get<PermissionOverviewModel[]>('/admin/permissions', { context: this.silentContext });
  }

  stateMachine(): Observable<StateMachineConfigModel> {
    return this.apiService.get<StateMachineConfigModel>('/admin/state-machine', { context: this.silentContext });
  }

  documentTypes(): Observable<DocumentTypeModel[]> {
    return this.apiService.get<DocumentTypeModel[]>('/admin/document-types', { context: this.silentContext });
  }

  updateDocumentType(id: number, body: unknown): Observable<DocumentTypeModel> {
    return this.apiService.put<DocumentTypeModel>(`/admin/document-types/${id}`, body);
  }

  reasonCodes(): Observable<ReasonCodeModel[]> {
    return this.apiService.get<ReasonCodeModel[]>('/admin/reason-codes', { context: this.silentContext });
  }

  createReasonCode(body: unknown): Observable<ReasonCodeModel> {
    return this.apiService.post<ReasonCodeModel>('/admin/reason-codes', body);
  }

  updateReasonCode(id: number, body: unknown): Observable<ReasonCodeModel> {
    return this.apiService.put<ReasonCodeModel>(`/admin/reason-codes/${id}`, body);
  }
}
