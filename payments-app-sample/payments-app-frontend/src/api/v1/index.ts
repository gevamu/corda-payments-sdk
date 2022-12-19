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
import {ApiV1} from 'src/api/v1/models/ApiV1.interface'
import {axiosInstance} from 'src/api/v1/axiosInstance';
import {
  GetCreditorsResponse, GetDebtorsResponse,
  GetPaymentsResponse,
  GetRegistrationResponse,
  RegisterResponse
} from 'src/api/v1/models/responses';

export const apiV1: ApiV1 = {
  async getDebtors(){
    const {data} = await axiosInstance.get<GetDebtorsResponse>('/participants/debtors')
    return data
  },
  async getCreditors(){
    const {data} = await axiosInstance.get<GetCreditorsResponse>('/participants/creditors')
    return data
  },
  async submitPayment(request) {
    const {data} = await axiosInstance.post<void>('/payments', request)
    return data
  },
  async getPayments(){
    const {data} = await axiosInstance.get<GetPaymentsResponse>('/payments/states')
    return data
  },
  async getRegistration(){
    const {data} = await axiosInstance.get<GetRegistrationResponse>('/registration')
    return data
  },
  async register(){
    const {data} = await axiosInstance.post<RegisterResponse>('/registration', {})
    return data
  }
}
