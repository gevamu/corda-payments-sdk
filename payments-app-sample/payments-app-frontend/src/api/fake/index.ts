/*
 * Copyright 2022 Exactpro Systems Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {ApiV1} from 'src/api/v1/models/ApiV1.interface'
import {sleep} from 'src/utils/sleep'
import {creditorsDb} from 'src/api/fake/db/creditors.db'
import {paymentsDb} from 'src/api/fake/db/payments.db'
import {registrationDb} from 'src/api/fake/db/registration.db';
import {debtorsDb} from 'src/api/fake/db/debtors.db';

export const fakeApi: ApiV1 = {
  async getDebtors(){
    await sleep(300)
    const accounts = [...debtorsDb]
    return {accounts}
  },
  async getCreditors(){
    await sleep(300)
    const accounts = [...creditorsDb]
    return {accounts}
  },
  async submitPayment(request){
    await sleep(300)
    const r = Math.random()
    paymentsDb.push({
      paymentId: `Payment ${paymentsDb.length + 1}`,
      status: r < 0.25 ? 'PENDING': r < 0.5 ? 'ACCEPTED': r < 0.75 ? 'REJECTED' : 'SENT_TO_GATEWAY',
      currency: 'USD',
      amount: request.amount,
      creditor: creditorsDb.find(cr => cr.accountId === request.creditorAccount) || creditorsDb[0],
      debtor: debtorsDb.find(cr => cr.accountId === request.debtorAccount) || debtorsDb[0],
      endToEndId: Array.from(Array(16)).map(() => Number(Math.floor(Math.random()*13)).toString(16)).join('') ,
      creationTime: new Date().toISOString(),
      updateTime: new Date().toISOString()
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
      networkId: 'TAAS0010000030',
      participantId: '363b72b48ada766f3c6',
      currency: 'USD'
    }
    return JSON.parse(JSON.stringify(registrationDb.registration))
  },
  async getRegistration(){
    await sleep(2000)
    return JSON.parse(JSON.stringify(registrationDb.registration))
  }
}
