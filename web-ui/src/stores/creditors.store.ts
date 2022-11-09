import {defineStore} from 'pinia'
import {CreditorAccount} from 'src/models/CreditorAccount.interface'
import {api} from 'src/api'

export const useCreditorsStore = defineStore('creditors', {
  state(){
    return {
      creditors: [] as CreditorAccount[]
    }
  },
  actions: {
    // TODO: fetch creditors only when authorised
    async fetchCreditors(){
      const result = await api.getCreditors()
      this.creditors = result.accounts.map(acc => ({
        id: acc.accountId,
        name: acc.accountName
      }))
    }
  }
})
