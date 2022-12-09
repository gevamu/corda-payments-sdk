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

import {SubmitPaymentRequest} from 'src/api/v1/models/requests/submitPayment.type'
import {GetCreditorsResponse} from 'src/api/v1/models/responses/getCreditors.type'
import {GetPaymentsResponse} from 'src/api/v1/models/responses/getPayments.type'
import {GetRegistrationResponse} from 'src/api/v1/models/responses/getRegistration.type';
import {RegisterResponse} from 'src/api/v1/models/responses/register.type';
import {GetDebtorsResponse} from 'src/api/v1/models/responses';

export interface ApiV1 {
  getDebtors: () => Promise<GetDebtorsResponse>
  getCreditors: () => Promise<GetCreditorsResponse>
  submitPayment: (request: SubmitPaymentRequest) => Promise<void>
  getPayments: () => Promise<GetPaymentsResponse>
  getRegistration: () => Promise<GetRegistrationResponse>
  register: () => Promise<RegisterResponse>
}
