export interface Payment {
  id: string
  endToEndId: string
  amount: number
  // TODO: specify currencies enum
  currency: string
  status: string
  creditor: string
  debtor: string
  creationTime: Date
  updateTime: Date
}
