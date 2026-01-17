export interface CoverLetterApplicationDto {
  jobTitle: string | undefined
  company: string | undefined
  contactPerson: CoverLetterContactPersonDto | undefined
  addressee: string | undefined
  salutation: string | undefined
  companyAddress: CoverLetterCompanyAddressDto | undefined
  coverLetterContent: string | undefined
  closing: string | undefined
}

export interface CoverLetterCompanyAddressDto {
  street: string | undefined
  postcode: string | undefined
  city: string | undefined
}

export interface CoverLetterContactPersonDto {
  firstName: string | undefined
  lastName: string | undefined
}
