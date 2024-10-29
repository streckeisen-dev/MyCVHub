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

async function getPublicProfile(alias: string): Promise<PublicProfileDto> {
  try {
    const response = await fetchFromApi(`/api/public/profile/${alias}`)
    const profile = await getJSONIfResponseIsOk<PublicProfileDto>(response)
    return Promise.resolve(profile)
  } catch (error) {
    return Promise.reject(error)
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
  } catch (error) {
    return Promise.reject(error)
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
  } catch (error) {
    return Promise.reject(error)
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
  } catch (error) {
    return Promise.reject(error)
  }
}

async function deleteWorkExperience(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/work-experience/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
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
  } catch (error) {
    return Promise.reject(error)
  }
}

async function deleteEducation(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/education/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
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
  } catch (error) {
    return Promise.reject(error)
  }
}

async function deleteSkill(id: number): Promise<void> {
  try {
    const response = await fetchFromApi(`/api/profile/skill/${id}`, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
  }
}

async function getThumbnail(): Promise<ThumbnailDto> {
  try {
    const response = await fetchFromApi('/profile/picture/thumbnail')
    const thumbnail = getJSONIfResponseIsOk<ThumbnailDto>(response)
    return Promise.resolve(thumbnail)
  } catch (error) {
    return Promise.reject(error)
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
  getThumbnail
}
