import {defineStore} from 'pinia'
import {Payment} from 'src/models/Payment.interface'
import {PaymentStatus as ApiPaymentStatus} from 'src/api/v1/models/PaymentState.type'
import {PaymentStatus} from 'src/models/payment/PaymentStatus.type'
import Timeout = NodeJS.Timeout

import {api} from 'src/api'

const status2Text = {
  SENT_TO_GATEWAY: 'Sent to Gateway',
  PENDING: 'Pending',
  CREATED: 'Created',
  COMPLETED: 'Completed',
  REJECTED: 'Rejected',
  ACCEPTED: 'Accepted',
}

export const usePaymentsStore = defineStore('payments', {
  state(){
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
      function mapStatus(apiStatus: ApiPaymentStatus): PaymentStatus {
        return status2Text[apiStatus] ?? 'Unknown'
      }
      this.payments = result.states.map(state => {
        return {
          creditor: state.creditor.accountId,
          debtor: state.debtor.accountId,
          amount: state.amount,
          currency: state.currency,
          status: mapStatus(state.status),
          id: state.paymentId
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
