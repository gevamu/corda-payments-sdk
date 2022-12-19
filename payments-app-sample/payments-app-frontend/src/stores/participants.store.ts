/*
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
 */

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
        name: acc.accountName,
        currency: acc.currency,
      }))
    },
    async fetchDebtors(){
      const result = await api.getDebtors()
      this.debtors = result.accounts.map(acc => ({
        id: acc.accountId,
        name: acc.accountName,
        currency: acc.currency,
      }))
    }
  }
})
