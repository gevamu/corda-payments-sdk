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

import {ParticipantAccount} from 'src/api/v1/models/ParticipantAccount.type';

export type PaymentState = {
  paymentId: string
  endToEndId: string
  amount: number
  // TODO: specify exact currency values
  currency: string
  status: PaymentStatus
  creditor: ParticipantAccount
  debtor: ParticipantAccount
  /**
   * ISO datetime string
   */
  creationTime: string
  /**
   * ISO datetime string
   */
  updateTime: string
}

export type PaymentStatus = 'CREATED' | 'SENT_TO_GATEWAY' | 'ACCEPTED' | 'PENDING' | 'COMPLETED' | 'REJECTED'
