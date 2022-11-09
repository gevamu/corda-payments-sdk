import {defineStore} from 'pinia'
import {Payment} from 'src/models/Payment.interface'
import {PaymentStatus as ApiPaymentStatus} from 'src/api/v1/models/PaymentState.type';
import {PaymentStatus} from 'src/models/payment/PaymentStatus.type';
import Timeout = NodeJS.Timeout

import {api} from 'src/api'


export const usePaymentsStore = defineStore('payments', {
  state(){
    return {
      payments: [] as Payment[],
      loading: false,
      fetchingProcess: null as null | Timeout
    }
  },
  actions:{
    async submitPayment(amount: number, creditorId: string){
      const result = await api.submitPayment({ amount, beneficiaryAccount: creditorId })
      return result
    },
    async fetchPayments(){
      this.loading = true
      const result = await api.getPayments()
      function mapStatus(apiStatus: ApiPaymentStatus): PaymentStatus{
        if (apiStatus === 'SENT_TO_GATEWAY') return 'Sent to Gateway'
        if (apiStatus === 'PENDING') return 'Pending'
        if (apiStatus === 'CREATED') return 'Created'
        if (apiStatus === 'COMPLETED') return 'Completed'
        if (apiStatus === 'REJECTED') return 'Rejected'
        if (apiStatus === 'ACCEPTED') return 'Accepted'
        return 'Invalid'
      }
      this.payments = result.states.map(state => {
        return {
          creditorId: state.beneficiary.accountId,
          amount: state.amount,
          currency: state.currency,
          status: mapStatus(state.status),
          id: state.paymentId
        }
      })
      this.loading = false
    },
    startConstantFetching(){
      this.fetchingProcess = setInterval(async () => {
        await this.fetchPayments()
      }, 1000)
    },
    stopConstantFetching(){
      if (this.fetchingProcess)
        clearInterval(this.fetchingProcess)
      this.fetchingProcess = null
    }
  }
})
