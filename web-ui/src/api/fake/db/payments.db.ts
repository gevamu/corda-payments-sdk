import {PaymentState} from 'src/api/v1/models/PaymentState.type'
import {creditorsDb} from 'src/api/fake/db/creditors.db'

const payments: PaymentState[] = []
for (let i = 0; i < 5; i++) {
  payments.push({
    paymentId: `Payment ${i+1}`,
    status: Math.random() > 0.5 ? 'PENDING': Math.random() > 0.5 ? 'ACCEPTED': Math.random() > 0.5 ? 'REJECTED' : 'SENT_TO_GATEWAY',
    currency: 'GBP',
    beneficiary: creditorsDb[Math.floor(Math.random() * creditorsDb.length)],
    amount: 40
  })
}

export const paymentsDb = payments
