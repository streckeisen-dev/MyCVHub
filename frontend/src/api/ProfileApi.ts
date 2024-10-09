import type { PublicProfileDto } from '@/dto/PublicProfileDto'
import { commonHeaders, fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper'
import type { ProfileDto } from '@/dto/ProfileDto'
import type { ProfileUpdateRequestDto } from '@/dto/ProfileUpdateRequestDto'
import type { WorkExperienceUpdateDto } from '@/dto/WorkExperienceUpdateDto'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'

async function getPublicProfile(alias: string): Promise<PublicProfileDto> {
  try {
    const response = await fetchFromApi(`/api/public/profile/${alias}`)
    const profile = await getJSONIfResponseIsOk<PublicProfileDto>(response)
    return Promise.resolve(profile)
  } catch (error) {
    return Promise.reject(error)
  }
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

async function updateGeneralInformation(profileUpdate: ProfileUpdateRequestDto): Promise<ProfileDto> {
  try {
    const response = await fetchFromApi('/api/profile', {
      method: 'POST',
      body: JSON.stringify(profileUpdate),
      headers: commonHeaders
    })
    const profile = await getJSONIfResponseIsOk<ProfileDto>(response)
    return Promise.resolve(profile)
  } catch (error) {
    return Promise.reject(error)
  }
}

async function saveWorkExperience(workExperienceUpdate: WorkExperienceUpdateDto): Promise<WorkExperienceDto> {
  try {
    const response = await fetchFromApi('/api/profile/work-experience', {
      method: 'POST',
      body: JSON.stringify(workExperienceUpdate),
      headers: commonHeaders
    })
    const workExperience = await getJSONIfResponseIsOk<WorkExperienceDto>(response)
    return Promise.resolve(workExperience)
  } catch (error) {
    return Promise.reject(error)
  }
}

async function deleteWorkExperience(id: number): Promise<void> {
  try {
    await fetchFromApi(`/api/profile/work-experience/${id}`, {
      method: 'DELETE'
    })
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
  }
}

export default {
  getPublicProfile,
  getProfile,
  updateGeneralInformation,
  saveWorkExperience,
  deleteWorkExperience
}