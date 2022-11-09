import {ApiV1} from 'src/api/v1/models/ApiV1.interface'
import {axiosInstance} from 'src/api/v1/axiosInstance';
import {
  GetCreditorsResponse,
  GetPaymentsResponse,
  GetRegistrationResponse,
  RegisterResponse
} from 'src/api/v1/models/responses';

export const apiV1: ApiV1 = {
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
