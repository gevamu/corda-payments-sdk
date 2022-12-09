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

const accounts: ParticipantAccount[] = [
  { accountId: '004531117838', accountName: 'XXXX XXXX XXXX XXXX', currency: 'USD' },
  { accountId: '260028006178', accountName: 'DFACSNSG ACU 260-028006-178', currency: 'USD' },
  { accountId: '40025050951366', accountName: 'ALIPAY GLOBA*BCB', currency: 'USD' },
  { accountId: '260183041178', accountName: 'DFACSNSG ACU 260-183041-178', currency: 'USD' },
  { accountId: '40032862001802', accountName: 'Alipay SINGAPORE E-COMMERCE PTE LTD', currency: 'USD' },
]
accounts.push()

export const creditorsDb = accounts
