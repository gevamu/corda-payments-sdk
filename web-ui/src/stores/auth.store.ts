import {defineStore} from 'pinia'
import {PayerProfile} from 'src/models/PayerProfile.interface'
import {api} from 'src/api'
import {useErrorHandler} from 'stores/errorHandler.store'

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
      try {
        const result = await api.register()
        this.payerProfile = result
      } catch (e) {
        const errorHandler = useErrorHandler()
        errorHandler.handleError(e)
      } finally {
        this.loading = false
      }
    },
    async getRegistration(){
      this.loading = true
      try {
        const result = await api.getRegistration()
        this.payerProfile = result
      } catch (e) {
        const errorHandler = useErrorHandler()
        errorHandler.handleError(e)
      } finally {
        this.loading = false
      }
    }
  },
  getters: {
    isAuthorized(): boolean {
      return !!this.payerProfile
    }
  }
})
