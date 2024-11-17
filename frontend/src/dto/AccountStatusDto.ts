export type AccountStatusDto = {
  status: AccountStatus
}

export enum AccountStatus {
  INCOMPLETE = 'INCOMPLETE',
  UNVERIFIED = 'UNVERIFIED',
  VERIFIED = 'VERIFIED'
}