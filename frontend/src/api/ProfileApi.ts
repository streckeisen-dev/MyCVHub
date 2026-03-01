import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { ProfileUpdateRequestDto } from '@/types/profile/ProfileUpdateRequestDto.ts'
import { WorkExperienceUpdateDto } from '@/types/profile/workExperience/WorkExperienceUpdateDto.ts'
import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import { EducationUpdateDto } from '@/types/profile/education/EducationUpdateDto.ts'
import { EducationDto } from '@/types/profile/education/EducationDto.ts'
import { ProjectUpdateDto } from '@/types/profile/project/ProjectUpdateDto.ts'
import { ProjectDto } from '@/types/profile/project/ProjectDto.ts'
import { SkillUpdateDto } from '@/types/profile/skill/SkillUpdateDto.ts'
import { SkillDto } from '@/types/profile/skill/SkillDto.ts'
import { ProfileThemeUpdateDto } from '@/types/profile/theme/ProfileThemeUpdateDto.ts'
import { ProfileThemeDto } from '@/types/profile/theme/ProfileThemeDto.ts'
import { PublicProfileDto } from '@/types/profile/PublicProfileDto.ts'

async function getProfile(locale: string): Promise<ProfileDto> {
  const response = await fetchFromApi('/profile', locale)
  return getJSONIfResponseIsOk<ProfileDto>(response)
}

async function saveGeneralInformation(profileUpdate: ProfileUpdateRequestDto, locale: string): Promise<ProfileDto> {
  const formData = new FormData()
  formData.append('data', JSON.stringify(profileUpdate))
  if (profileUpdate.profilePicture) {
    formData.append('profilePicture', profileUpdate.profilePicture)
  }
  const headers = new Headers()
  headers.append('Content-Type', '')
  const response = await fetchFromApi('/profile', locale, {
    method: 'POST',
    body: formData,
    headers: headers
  })
  return getJSONIfResponseIsOk<ProfileDto>(response)
}

async function saveWorkExperience(
  workExperienceUpdate: WorkExperienceUpdateDto,
  locale: string
): Promise<WorkExperienceDto> {
  const response = await fetchFromApi('/profile/work-experience', locale, {
    method: 'POST',
    body: JSON.stringify(workExperienceUpdate)
  })
  return getJSONIfResponseIsOk<WorkExperienceDto>(response)
}

async function deleteWorkExperience(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/profile/work-experience/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function saveEducation(educationUpdate: EducationUpdateDto, locale: string): Promise<EducationDto> {
  const response = await fetchFromApi('/profile/education', locale, {
    method: 'POST',
    body: JSON.stringify(educationUpdate)
  })
  return getJSONIfResponseIsOk<EducationDto>(response)
}

async function deleteEducation(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/profile/education/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function saveProject(projectUpdate: ProjectUpdateDto, locale: string): Promise<ProjectDto> {
  const response = await fetchFromApi('/profile/project', locale, {
    method: 'POST',
    body: JSON.stringify(projectUpdate)
  })
  return getJSONIfResponseIsOk<ProjectDto>(response)
}

async function deleteProject(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/profile/project/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function saveSkill(skillUpdate: SkillUpdateDto, locale: string): Promise<SkillDto> {
  const response = await fetchFromApi('/profile/skill', locale, {
    method: 'POST',
    body: JSON.stringify(skillUpdate)
  })
  return getJSONIfResponseIsOk<SkillDto>(response)
}

async function deleteSkill(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/profile/skill/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function saveTheme(themeUpdate: ProfileThemeUpdateDto, locale: string): Promise<ProfileThemeDto> {
  const response = await fetchFromApi('/profile/theme', locale, {
    method: 'POST',
    body: JSON.stringify(themeUpdate)
  })
  return getJSONIfResponseIsOk<ProfileThemeDto>(response)
}

async function getPublicProfile(username: string, locale: string): Promise<PublicProfileDto> {
  const response = await fetchFromApi(`/public/profile/${username}`, locale)
  return getJSONIfResponseIsOk<PublicProfileDto>(response)
}

export default {
  getProfile,
  saveGeneralInformation,
  saveWorkExperience,
  deleteWorkExperience,
  saveEducation,
  deleteEducation,
  saveProject,
  deleteProject,
  saveSkill,
  deleteSkill,
  saveTheme,
  getPublicProfile
}
