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
    async getRegistration() {
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
