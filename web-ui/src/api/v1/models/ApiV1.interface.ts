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