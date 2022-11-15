import {PaymentState} from 'src/api/v1/models/PaymentState.type'
import {creditorsDb} from 'src/api/fake/db/creditors.db'
import {debtorsDb} from 'src/api/fake/db/debtors.db'

const payments: PaymentState[] = []
// for (let i = 0; i < 5; i++) {
//   payments.push({
//     paymentId: `Payment ${i+1}`,
//     status: Math.random() > 0.5 ? 'PENDING': Math.random() > 0.5 ? 'ACCEPTED': Math.random() > 0.5 ? 'REJECTED' : 'SENT_TO_GATEWAY',
//     currency: 'GBP',
//     creditor: creditorsDb[Math.floor(Math.random() * creditorsDb.length)],
//     debtor: debtorsDb[Math.floor(Math.random() * debtorsDb.length)],
//     amount: 40,
//     endToEndId: '234567',
//     creationTime: new Date().toISOString(),
//     updateTime: new Date().toISOString()
//   })
// }

export const paymentsDb = payments
