export interface AccountDto {
  username: string
  firstName: string
  lastName: string
  email: string
  phone: string
  birthday: string
  street: string
  houseNumber?: string
  postcode: string
  city: string
  country: string
  language: string
  hasProfile: boolean,
  hasPassword: boolean,
  oauthIntegrations: LinkedAccountDto[]
}

export interface LinkedAccountDto {
  provider: string
}
