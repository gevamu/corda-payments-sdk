import {ParticipantAccount} from 'src/api/v1/models/ParticipantAccount.type';

export type PaymentState = {
  paymentId: string
  amount: number
  // TODO: specify exact currency values
  currency: string
  status: PaymentStatus
  creditor: ParticipantAccount
  debtor: ParticipantAccount
}

export type PaymentStatus = 'CREATED' | 'SENT_TO_GATEWAY' | 'ACCEPTED' | 'PENDING' | 'COMPLETED' | 'REJECTED'

