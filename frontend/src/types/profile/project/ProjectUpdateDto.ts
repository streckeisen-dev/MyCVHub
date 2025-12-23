import { ProjectLinkUpdateDto } from '@/types/profile/project/ProjectLinkUpdateDto.ts'

export interface ProjectUpdateDto {
  id?: number;
  name?: string;
  role?: string;
  description?: string;
  projectStart?: string;
  projectEnd?: string;
  links?: ProjectLinkUpdateDto[];
}
