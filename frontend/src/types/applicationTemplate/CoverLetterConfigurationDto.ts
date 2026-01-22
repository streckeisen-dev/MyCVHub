export interface CoverLetterConfigurationDto {
  style: string
  language: string
  mirrorProfileImage: boolean
  content: string
  closing: string
  documents: string[] | undefined
}