export type ErrorDto = {
  message: string
  errors: { [key: string]: string }
  status: number
}
