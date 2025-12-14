import { ProfileDto } from '@/types/ProfileDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { ProfileUpdateRequestDto } from '@/types/ProfileUpdateRequestDto.ts'
import { WorkExperienceUpdateDto } from '@/types/WorkExperienceUpdateDto.ts'
import { WorkExperienceDto } from '@/types/WorkExperienceDto.ts'
import { EducationUpdateDto } from '@/types/EducationUpdateDto.ts'
import { EducationDto } from '@/types/EducationDto.ts'
import { ProjectUpdateDto } from '@/types/ProjectUpdateDto.ts'
import { ProjectDto } from '@/types/ProjectDto.ts'
import { SkillUpdateDto } from '@/types/SkillUpdateDto.ts'
import { SkillDto } from '@/types/SkillDto.ts'
import { ProfileThemeUpdateDto } from '@/types/ProfileThemeUpdateDto.ts'
import { ProfileThemeDto } from '@/types/ProfileThemeDto.ts'
import { PublicProfileDto } from '@/types/PublicProfileDto.ts'

async function getProfile(locale: string): Promise<ProfileDto> {
  try {
    const response = await fetchFromApi('/profile', locale)
    return await getJSONIfResponseIsOk<ProfileDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load profile', error)
  }
}

async function saveGeneralInformation(profileUpdate: ProfileUpdateRequestDto, locale: string): Promise<ProfileDto> {
  const formData = new FormData()
  formData.append('data', JSON.stringify(profileUpdate))
  if (profileUpdate.profilePicture) {
    formData.append('profilePicture', profileUpdate.profilePicture)
  }
  const headers = new Headers()
  headers.append('Content-Type', '')
  try {
    const response = await fetchFromApi('/profile', locale, {
      method: 'POST',
      body: formData,
      headers: headers
    })
    return await getJSONIfResponseIsOk<ProfileDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to update CV profile', error)
  }
}

async function saveWorkExperience(
  workExperienceUpdate: WorkExperienceUpdateDto,
  locale: string
): Promise<WorkExperienceDto> {
  try {
    const response = await fetchFromApi('/profile/work-experience', locale, {
      method: 'POST',
      body: JSON.stringify(workExperienceUpdate)
    })
    return await getJSONIfResponseIsOk<WorkExperienceDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save work experience', error)
  }
}

async function deleteWorkExperience(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/profile/work-experience/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete work experience', error)
  }
}

async function saveEducation(educationUpdate: EducationUpdateDto, locale: string): Promise<EducationDto> {
  try {
    const response = await fetchFromApi('/profile/education', locale, {
      method: 'POST',
      body: JSON.stringify(educationUpdate)
    })
    return await getJSONIfResponseIsOk<EducationDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save education', error)
  }
}

async function deleteEducation(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/profile/education/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete education', error)
  }
}

async function saveProject(projectUpdate: ProjectUpdateDto, locale: string): Promise<ProjectDto> {
  try {
    const response = await fetchFromApi('/profile/project', locale, {
      method: 'POST',
      body: JSON.stringify(projectUpdate)
    })
    return await getJSONIfResponseIsOk<ProjectDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save project', error)
  }
}

async function deleteProject(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/profile/project/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete project', error)
  }
}

async function saveSkill(skillUpdate: SkillUpdateDto, locale: string): Promise<SkillDto> {
  try {
    const response = await fetchFromApi('/profile/skill', locale, {
      method: 'POST',
      body: JSON.stringify(skillUpdate)
    })
    return await getJSONIfResponseIsOk<SkillDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save skill', error)
  }
}

async function deleteSkill(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/profile/skill/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete skill', error)
  }
}

async function saveTheme(themeUpdate: ProfileThemeUpdateDto, locale: string): Promise<ProfileThemeDto> {
  try {
    const response = await fetchFromApi('/profile/theme', locale, {
      method: 'POST',
      body: JSON.stringify(themeUpdate)
    })
    return await getJSONIfResponseIsOk<ProfileThemeDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save theme', error)
  }
}

async function getPublicProfile(username: string, locale: string): Promise<PublicProfileDto> {
  try {
    const response = await fetchFromApi(`/public/profile/${username}`, locale)
    return await getJSONIfResponseIsOk<PublicProfileDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load public profile', error)
  }
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