import {PaymentStatus} from 'src/models/payment/PaymentStatus.type';

export interface Payment {
  id: string
  amount: number
  // TODO: specify currencies enum
  currency: string
  status: PaymentStatus
  creditor: string
}
