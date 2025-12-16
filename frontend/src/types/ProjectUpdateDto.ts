import { ProjectLinkUpdateDto } from '@/types/ProjectLinkUpdateDto.ts'

export interface ProjectUpdateDto {
  id?: number;
  name?: string;
  role?: string;
  description?: string;
  projectStart?: string;
  projectEnd?: string;
  links?: ProjectLinkUpdateDto[];
}
