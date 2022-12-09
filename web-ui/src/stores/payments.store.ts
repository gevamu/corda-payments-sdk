/*******************************************************************************
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
 ******************************************************************************/

import {defineStore} from 'pinia'
import {Payment} from 'src/models/Payment.interface'
import {PaymentStatus} from 'src/api/v1/models/PaymentState.type'
import Timeout = NodeJS.Timeout

import {api} from 'src/api'
// import {useErrorHandler} from 'stores/errorHandler.store'
//
// const errorHandler = useErrorHandler()

const status2Text: Record<PaymentStatus, string> = {
  SENT_TO_GATEWAY: 'Sent to Gateway',
  PENDING: 'Pending',
  CREATED: 'Created',
  COMPLETED: 'Completed',
  REJECTED: 'Rejected',
  ACCEPTED: 'Accepted',
}

export const usePaymentsStore = defineStore('payments', {
  state() {
    return {
      payments: [] as Payment[],
      loading: false,
      fetchingProcess: null as null | Timeout
    }
  },
  actions: {
    async submitPayment(debtorId: string, creditorId: string, amount: number) {
      const result = await api.submitPayment({
        amount,
        creditorAccount: creditorId,
        debtorAccount: debtorId
      })
      return result
    },
    async fetchPayments() {
      this.loading = true
      const result = await api.getPayments()
      function mapStatus(apiStatus: PaymentStatus): string {
        return status2Text[apiStatus] ?? 'Unknown'
      }
      this.payments = result.states.map(state => {
        return {
          creditor: state.creditor.accountId,
          debtor: state.debtor.accountId,
          amount: state.amount,
          currency: state.currency,
          status: mapStatus(state.status),
          id: state.paymentId,
          endToEndId: state.endToEndId,
          creationTime: new Date(state.creationTime),
          updateTime: new Date(state.updateTime)
        }
      })
      this.loading = false
    },
    startConstantFetching() {
      this.fetchingProcess = setInterval(async () => {
        await this.fetchPayments()
      }, 1000)
    },
    stopConstantFetching() {
      if (this.fetchingProcess)
        clearInterval(this.fetchingProcess)
      this.fetchingProcess = null
    }
  }
})
