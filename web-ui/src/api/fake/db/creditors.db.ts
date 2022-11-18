import {ParticipantAccount} from 'src/api/v1/models/ParticipantAccount.type';

const accounts: ParticipantAccount[] = [
  { accountId: '004531117838', accountName: 'XXXX XXXX XXXX XXXX', currency: 'USD' },
  { accountId: '260028006178', accountName: 'DFACSNSG ACU 260-028006-178', currency: 'USD' },
  { accountId: '40025050951366', accountName: 'ALIPAY GLOBA*BCB', currency: 'USD' },
  { accountId: '260183041178', accountName: 'DFACSNSG ACU 260-183041-178', currency: 'USD' },
  { accountId: '40032862001802', accountName: 'Alipay SINGAPORE E-COMMERCE PTE LTD', currency: 'USD' },
]
accounts.push()
// for (let i = 0; i < 5; i++){
//   accounts.push({
//     accountId: Math.round(Math.random()*10000).toString(),
//     accountName: `Participant ${i+1}`
//   })
// }

export const creditorsDb = accounts
