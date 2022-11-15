import {defineStore} from 'pinia'

export const useErrorHandler = defineStore('errorHandler', {
  state(){
    return {
      error: null as null | Error
    }
  },
  actions: {
    handleError(error: Error){
      this.error = error
    },
    hideError(){
      this.error = null
    }
  }
})
