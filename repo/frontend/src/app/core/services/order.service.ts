import { Injectable } from '@angular/core';
import { HttpContext, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ManagedReasonCodeModel, OrderDetailModel, OrderSummaryModel, ProductCatalogModel } from '../models/order.models';
import { PageResponse, PageParams } from '../models/page.model';
import { SKIP_ERROR_TOAST } from '../interceptors/http-context.tokens';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly silentContext = new HttpContext().set(SKIP_ERROR_TOAST, true);

  constructor(private readonly apiService: ApiService) {}

  listCatalog(): Observable<ProductCatalogModel[]> {
    return this.apiService.get<ProductCatalogModel[]>('/catalog/products', { context: this.silentContext });
  }

  listOrders(params?: PageParams & { status?: string }): Observable<PageResponse<OrderSummaryModel>> {
    let httpParams = new HttpParams();
    if (params?.page != null) httpParams = httpParams.set('page', params.page);
    if (params?.size != null) httpParams = httpParams.set('size', params.size);
    if (params?.sort) httpParams = httpParams.set('sort', params.sort);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    return this.apiService.get<PageResponse<OrderSummaryModel>>('/orders', { context: this.silentContext, params: httpParams });
  }

  getOrder(orderId: number): Observable<OrderDetailModel> {
    return this.apiService.get<OrderDetailModel>(`/orders/${orderId}`, { context: this.silentContext });
  }

  listReasonCodes(codeType: 'RETURN' | 'AFTER_SALES'): Observable<ManagedReasonCodeModel[]> {
    return this.apiService.get<ManagedReasonCodeModel[]>('/orders/reason-codes', { context: this.silentContext, params: new HttpParams().set('codeType', codeType) });
  }

  createOrder(body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>('/orders', body);
  }

  submitForReview(orderId: number): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/submit-review`, {});
  }

  cancelOrder(orderId: number): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/cancel`, {});
  }

  approve(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/approve`, body);
  }

  recordPayment(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/record-payment`, body);
  }

  pickPack(orderId: number): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/pick-pack`, {});
  }

  createShipment(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/shipments`, body);
  }

  createReceipt(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/receipts`, body);
  }

  createReturn(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/returns`, body);
  }

  createAfterSalesCase(orderId: number, body: unknown): Observable<OrderDetailModel> {
    return this.apiService.post<OrderDetailModel>(`/orders/${orderId}/after-sales-cases`, body);
  }
}
