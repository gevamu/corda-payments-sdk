import {defineStore} from 'pinia'
import {ParticipantAccount} from 'src/models/ParticipantAccount.interface'
import {api} from 'src/api'

export const useParticipantsStore = defineStore('participants', {
  state(){
    return {
      creditors: [] as ParticipantAccount[],
      debtors: [] as ParticipantAccount[]
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
    },
    async fetchDebtors(){
      const result = await api.getDebtors()
      this.debtors = result.accounts.map(acc => ({
        id: acc.accountId,
        name: acc.accountName
      }))
    }
  }
})
