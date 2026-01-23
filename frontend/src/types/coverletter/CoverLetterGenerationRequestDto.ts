import { CoverLetterApplicationDto } from '@/types/coverletter/CoverLetterApplicationDto.ts'

export interface CoverLetterGenerationRequestDto {
  language: string | undefined
  style: string | undefined
  mirrorProfileImage: boolean | undefined
  application: CoverLetterApplicationDto | undefined
  documents: string[] | undefined
}