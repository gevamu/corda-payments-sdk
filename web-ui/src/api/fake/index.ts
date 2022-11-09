import {ApiV1} from 'src/api/v1/models/ApiV1.interface'
import {sleep} from 'src/utils/sleep'
import {creditorsDb} from 'src/api/fake/db/creditors.db'
import {paymentsDb} from 'src/api/fake/db/payments.db'
import {registrationDb} from 'src/api/fake/db/registration.db';

export const fakeApi: ApiV1 = {
  async getCreditors(){
    await sleep(300)
    const accounts = [...creditorsDb]
    return {accounts}
  },
  async submitPayment(request){
    await sleep(300)
    paymentsDb.push({
      paymentId: `Payment ${paymentsDb.length + 1}`,
      status: Math.random() > 0.5 ? 'PENDING': Math.random() > 0.5 ? 'ACCEPTED': Math.random() > 0.5 ? 'REJECTED' : 'SENT_TO_GATEWAY',
      currency: 'GBP',
      amount: request.amount,
      beneficiary: creditorsDb.find(cr => cr.accountId === request.beneficiaryAccount) || creditorsDb[0]
    })
    return
  },
  async getPayments(){
    await sleep(300)
    const payments = [...paymentsDb]
    return { states: payments }
  },
  async register(){
    await sleep(300)
    registrationDb.registration = {
      networkId: 'qwertyu',
      participantId: 'qwerty'
    }
    return JSON.parse(JSON.stringify(registrationDb.registration))
  },
  async getRegistration(){
    await sleep(2000)
    return JSON.parse(JSON.stringify(registrationDb.registration))
  }
}
