import type { PublicProfileDto } from '@/dto/PublicProfileDto'
import {
  commonHeaders,
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper'
import type { ProfileDto } from '@/dto/ProfileDto'
import type { ProfileUpdateRequestDto } from '@/dto/ProfileUpdateRequestDto'
import type { WorkExperienceUpdateDto } from '@/dto/WorkExperienceUpdateDto'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import type { EducationUpdateDto } from '@/dto/EducationUpdateDto'
import type { EducationDto } from '@/dto/EducationDto'
import type { SkillUpdateDto } from '@/dto/SkillUpdateDto'
import type { SkillDto } from '@/dto/SkillDto'
import type { ThumbnailDto } from '@/dto/ThumbnailDto'
import type { ProfileThemeUpdateDto } from '@/dto/ProfileThemeUpdateDto'
import type { ProfileThemeDto } from '@/dto/ProfileThemeDto'
import { ProjectUpdateDto } from '@/dto/ProjectUpdateDto'
import { ProjectDto } from '@/dto/ProjectDto'
import { RestError } from '@/api/RestError'

async function getPublicProfile(username: string): Promise<PublicProfileDto> {
  try {
    const response = await fetchFromApi(`/api/public/profile/${username}`)
    const profile = await getJSONIfResponseIsOk<PublicProfileDto>(response)
    return Promise.resolve(profile)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to load public profile', error))
  }
}

function getDefaultProfilePicture(): string {
  return new URL('@/assets/default_profile_picture.png', import.meta.url).toString()
}

function getDefaultProfilePictureThumbnail(): string {
  return new URL('@/assets/default_profile_picture_thumbnail.png', import.meta.url).toString()
}

async function getProfile(): Promise<ProfileDto> {
  try {
    const response = await fetchFromApi('/api/profile')
    const profile = await getJSONIfResponseIsOk<ProfileDto>(response)
    return Promise.resolve(profile)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to load profile', error))
  }
}

async function updateGeneralInformation(
  profileUpdate: ProfileUpdateRequestDto
): Promise<ProfileDto> {
  const formData = new FormData()
  formData.append('data', JSON.stringify(profileUpdate))
  if (profileUpdate.profilePicture) {
    formData.append('profilePicture', profileUpdate.profilePicture)
  }
  try {
    const response = await fetchFromApi('/api/profile', {
      method: 'POST',
      body: formData
    })
    const profile = await getJSONIfResponseIsOk<ProfileDto>(response)
    return Promise.resolve(profile)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to update CV profile', error))
  }
}

async function saveWorkExperience(
  workExperienceUpdate: WorkExperienceUpdateDto
): Promise<WorkExperienceDto> {
  try {
    const response = await fetchFromApi('/api/profile/work-experience', {
      method: 'POST',
      body: JSON.stringify(workExperienceUpdate),
      headers: commonHeaders()
    })
    const workExperience = await getJSONIfResponseIsOk<WorkExperienceDto>(response)
    return Promise.resolve(workExperience)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to save work experience', error))
  }
}

async function deleteWorkExperience(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/work-experience/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to delete work experience', error))
  }
}

async function saveEducation(educationUpdate: EducationUpdateDto): Promise<EducationDto> {
  try {
    const response = await fetchFromApi('/api/profile/education', {
      method: 'POST',
      body: JSON.stringify(educationUpdate),
      headers: commonHeaders()
    })
    const education = await getJSONIfResponseIsOk<EducationDto>(response)
    return Promise.resolve(education)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to save education', error))
  }
}

async function deleteEducation(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/education/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to delete education', error))
  }
}

async function saveSkill(skillUpdate: SkillUpdateDto): Promise<SkillDto> {
  try {
    const response = await fetchFromApi('/api/profile/skill', {
      method: 'POST',
      body: JSON.stringify(skillUpdate),
      headers: commonHeaders()
    })
    const skill = await getJSONIfResponseIsOk<SkillDto>(response)
    return Promise.resolve(skill)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to save skill', error))
  }
}

async function deleteSkill(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/skill/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to delete skill', error))
  }
}

async function saveProject(projectUpdate: ProjectUpdateDto): Promise<ProjectDto> {
  try {
    const response = await fetchFromApi('/api/profile/project', {
      method: 'POST',
      body: JSON.stringify(projectUpdate),
      headers: commonHeaders()
    })
    const project = await getJSONIfResponseIsOk<ProjectDto>(response)
    return Promise.resolve(project)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to save project', error))
  }
}

async function deleteProject(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/project/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to delete project', error))
  }
}

async function getThumbnail(): Promise<ThumbnailDto> {
  try {
    const response = await fetchFromApi('/profile/picture/thumbnail')
    const thumbnail = await getJSONIfResponseIsOk<ThumbnailDto>(response)
    return Promise.resolve(thumbnail)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to load thumbnail', error))
  }
}

async function saveTheme(themeUpdate: ProfileThemeUpdateDto): Promise<ProfileThemeDto> {
  try {
    const response = await fetchFromApi('/api/profile/theme', {
      method: 'POST',
      body: JSON.stringify(themeUpdate),
      headers: commonHeaders()
    })
    const updatedTheme = await getJSONIfResponseIsOk<ProfileThemeDto>(response)
    return Promise.resolve(updatedTheme)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to save theme', error))
  }
}

export default {
  getPublicProfile,
  getDefaultProfilePicture,
  getDefaultProfilePictureThumbnail,
  getProfile,
  updateGeneralInformation,
  saveWorkExperience,
  deleteWorkExperience,
  saveEducation,
  deleteEducation,
  saveSkill,
  deleteSkill,
  saveProject,
  deleteProject,
  getThumbnail,
  saveTheme
}
