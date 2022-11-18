import {defineStore} from 'pinia'

export const useErrorHandler = defineStore('errorHandler', {
  state() {
    return {
      error: null as null | string
    }
  },
  actions: {
    handleError(error: unknown) {
      if (error instanceof Error) {
        this.error = error.message
      } else {
        this.error = `${error}`
      }
    },
    hideError() {
      this.error = null
    }
  }
})
