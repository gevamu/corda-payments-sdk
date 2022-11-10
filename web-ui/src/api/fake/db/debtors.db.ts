import {ParticipantAccount} from 'src/api/v1/models/ParticipantAccount.type';

const debtors: ParticipantAccount[] = []
for (let i = 0; i < 5; i++){
  debtors.push({
    accountId: Math.round(Math.random()*10000).toString(),
    accountName: `Participant ${i+1}`
  })
}

export const debtorsDb = debtors
