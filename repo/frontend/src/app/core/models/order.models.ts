export interface ProductCatalogModel {
  id: number;
  sku: string;
  name: string;
  unitPrice: number;
  unit: string;
}

export interface OrderSummaryModel {
  id: number;
  orderNumber: string;
  status: string;
  buyer: string;
  organizationCode?: string;
  createdAt: string;
  totalOrderedQuantity: number;
  totalShippedQuantity: number;
  totalReceivedQuantity: number;
  discrepancyOpen: boolean;
}

export interface OrderItemModel {
  id: number;
  productId: number;
  sku: string;
  name: string;
  unit: string;
  unitPrice: number;
  orderedQuantity: number;
  shippedQuantity: number;
  receivedQuantity: number;
  returnedQuantity: number;
  remainingToShip: number;
  remainingToReceive: number;
  discrepancyFlag: boolean;
}

export interface ReviewModel {
  decision: string;
  reviewer: string;
  comments: string;
  reviewedAt: string;
}

export interface PaymentModel {
  referenceNumber: string;
  amount: number;
  financeUser: string;
  recordedAt: string;
}

export interface ShipmentItemModel {
  orderItemId: number;
  sku: string;
  quantity: number;
}

export interface ShipmentModel {
  id: number;
  shipmentNumber: string;
  actor: string;
  shippedAt: string;
  notes: string;
  items: ShipmentItemModel[];
}

export interface ReceiptItemModel {
  orderItemId: number;
  sku: string;
  quantity: number;
  discrepancyReason?: string;
}

export interface ReceiptModel {
  id: number;
  receiptNumber: string;
  actor: string;
  receivedAt: string;
  hasDiscrepancy: boolean;
  notes: string;
  items: ReceiptItemModel[];
}

export interface ReturnItemModel {
  orderItemId: number;
  sku: string;
  quantity: number;
}

export interface ReturnModel {
  id: number;
  returnNumber: string;
  actor: string;
  reasonCode: string;
  comments: string;
  createdAt: string;
  items: ReturnItemModel[];
}

export interface AfterSalesCaseModel {
  id: number;
  caseNumber: string;
  orderItemId?: number;
  reasonCode: string;
  structuredDetail: string;
  createdAt: string;
}

export interface TraceabilityEventModel {
  eventType: string;
  fromStatus?: string;
  toStatus: string;
  actor: string;
  detail: string;
  createdAt: string;
}

export interface OrderDetailModel {
  id: number;
  orderNumber: string;
  status: string;
  buyer: string;
  organizationCode?: string;
  notes: string;
  paymentRecorded: boolean;
  createdAt: string;
  items: OrderItemModel[];
  review?: ReviewModel;
  payment?: PaymentModel;
  shipments: ShipmentModel[];
  receipts: ReceiptModel[];
  returns: ReturnModel[];
  afterSalesCases: AfterSalesCaseModel[];
  timeline: TraceabilityEventModel[];
}
