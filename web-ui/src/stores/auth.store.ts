import {defineStore} from 'pinia'
import {PayerProfile} from 'src/models/PayerProfile.interface';
import {api} from 'src/api';

export const useAuthStore = defineStore('auth', {
  state(){
    return {
      payerProfile: null as PayerProfile | null,
      loading: false
    }
  },
  actions: {
    async register() {
      this.loading = true
      const result = await api.register()
      this.payerProfile = result
      this.loading = false
    },
    async getRegistration(){
      this.loading = true
      const result = await api.getRegistration()
      this.payerProfile = result
      this.loading = false
    }
  },
  getters: {
    isAuthorized(): boolean {
      return !!this.payerProfile
    }
  }
})
