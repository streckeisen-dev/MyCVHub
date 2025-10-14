<template>
  <v-main>
    <v-container>
      <v-row
        align="center"
        justify="center"
      >
        <v-col
          cols="12"
          class="text-center"
        >
          <h1>{{ t('cv.generate') }}</h1>
          <p>{{ t('cv.intro') }}</p>
        </v-col>
      </v-row>
      <v-row
        justify="center"
        align="center"
      >
        <v-col
          cols="12"
          class="text-center"
        >
          <h2>{{ t('cv.style') }}</h2>
          <p>{{ t('cv.styleExplanation') }}</p>
        </v-col>
        <v-col
          v-for="style in cvStyles"
          :key="style.key"
          cols="12"
          sm="6"
          md="4"
          xl="2"
          class="text-center"
        >
          <v-card
            elevation="5"
            rounded
            variant="outlined"
            :class="cvStyleClasses(style.key).value"
          >
            <v-img :src="getStyleImage(style.key)" />
            <v-card-title>{{ style.name }}</v-card-title>
            <v-card-text v-html="style.description" />
            <v-card-actions>
              <v-btn
                :text="t('cv.select')"
                @click="onStyleSelected(style.key)"
              />
            </v-card-actions>
          </v-card>
        </v-col>
        <!--<v-col
          cols="12"
          sm="6"
          md="4"
          xl="2"
          class="text-center"
        >
          <v-card
            elevation="5"
            rounded
            variant="outlined"
          >
            <v-img src="https://dummyimage.com/800x1000/ffffff/000000.jpg&text=Coming+Soon" />
            <v-card-title>{{ t('cv.moreSoon') }}</v-card-title>
            <v-card-text>{{ t('cv.moreSoonText') }}</v-card-text>
          </v-card>
        </v-col>-->
      </v-row>
      <v-row
        justify="center"
        align="center"
      >
        <v-col
          cols="12"
          md="6"
          class="text-center cv-customize-content"
        >
          <v-btn
            @click="toggleCustomizeContent"
            variant="text"
          >
            <v-icon icon="mdi-tune" />
            {{ t('cv.customizeContent') }}
          </v-btn>
          <v-treeview
            class="text-left"
            v-if="customizeCVContent"
            v-model:selected="includedCVItems"
            :items="cvItems"
            select-strategy="classic"
            item-value="id"
            selectable
            bg-color="background"
          >
            <template #append="{ item }">
              <v-switch
                class="cv-item-description-switch"
                color="primary"
                :label="t('cv.showDescription')"
                v-if="item.hasDescription"
                v-model="cvItemDescriptionMap[item.id]"
                density="compact"
              />
            </template>
          </v-treeview>
        </v-col>
        <v-col
          v-if="(selectedCVStyle?.options || []).length > 0"
          cols="12"
          md="6"
          class="text-center cv-customize-template"
        >
          <v-btn
            @click="toggleCustomizeTemplate"
            variant="text"
          >
            <v-icon icon="mdi-tune" />
            {{ t('cv.customizeTemplate') }}
          </v-btn>

          <div v-if="customizeCVTemplate">
            <cv-style-option
              v-for="option in selectedCVStyle?.options"
              :key="option.key"
              v-model="cvTemplateOptions[option.key]"
              :option="option"
            />
          </div>
        </v-col>
      </v-row>
      <v-row
        justify="center"
        align="center"
      >
        <v-col
          cols="12"
          class="text-center"
        >
          <v-btn
            color="primary"
            :disabled="selectedStyle == null"
            @click="generateCV"
            :text="t('cv.download')"
            :flat="false"
            :loading="isGenerating"
          />
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ComputedRef, ref } from 'vue'
import { CVStyleDto } from '@/dto/CVStyleDto.ts'
import CVApi from '@/api/CVApi.ts'
import toastService from '@/services/ToastService.ts'
import { RestError } from '@/api/RestError.ts'
import { filename } from 'pathe/utils'
import ProfileApi from '@/api/ProfileApi.ts'
import CvStyleOption from '@/views/cv/CvStyleOption.vue'
import { CVGenerationRequestDto, IncludedCVItem } from '@/dto/CVGenerationRequestDto.ts'
import { KeyValueObject } from '@/model/KeyValueObject.ts'

interface CVTreeItem {
  id: string
  title: string
  hasDescription?: boolean
  children?: Array<CVTreeItem>
}

const { t } = useI18n({
  useScope: 'global'
})

const selectedStyle = ref<string>()
const selectedCVStyle = computed(() => cvStyles.value.find((s) => s.key === selectedStyle.value))
const cvStyles = ref<Array<CVStyleDto>>([])

const cvStyleImagesGlob = import.meta.glob<any>('@/assets/cv_styles/*.jpg', { eager: true })
const cvStyleImages = Object.fromEntries(
  Object.entries(cvStyleImagesGlob).map(([key, value]) => [filename(key), value.default])
)

const customizeCVContent = ref(false)
const includedCVItems = ref<Array<string>>(['workExperiences', 'education', 'projects', 'skills'])
const cvItems = ref<Array<CVTreeItem>>([
  {
    id: 'workExperiences',
    title: t('workExperience.title'),
    children: []
  },
  {
    id: 'education',
    title: t('education.title'),
    children: []
  },
  {
    id: 'projects',
    title: t('project.title'),
    children: []
  },
  {
    id: 'skills',
    title: t('skills.title'),
    children: []
  }
])
const cvItemDescriptionMap = ref<KeyValueObject<boolean>>({})

const customizeCVTemplate = ref(false)
const cvTemplateOptions = ref<KeyValueObject<string>>({})

const isGenerating = ref(false)

try {
  cvStyles.value = await CVApi.getCVStyles()
} catch (e) {
  const error = (e as RestError).errorDto
  const errorDetails = error?.message || t('error.genericMessage')
  toastService.error(t('cv.styleError', errorDetails))
}

try {
  const profile = await ProfileApi.getProfile()
  cvItems.value[0].children = profile.workExperiences.map((workExperience) => {
    const id = `work_${workExperience.id}`
    const hasDescription = workExperience.description != null
    const item: CVTreeItem = {
      id,
      title: `${workExperience.jobTitle} @ ${workExperience.company}`,
      hasDescription
    }
    includedCVItems.value.push(id)
    cvItemDescriptionMap.value[id] = hasDescription
    return item
  })

  cvItems.value[1].children = profile.education.map((edu) => {
    const id = `edu_${edu.id}`
    const hasDescription = edu.description != null
    const item: CVTreeItem = {
      id,
      title: `${edu.degreeName} @ ${edu.institution}`,
      hasDescription
    }
    includedCVItems.value.push(id)
    cvItemDescriptionMap.value[id] = hasDescription
    return item
  })

  cvItems.value[2].children = profile.projects.map((project) => {
    const id = `proj_${project.id}`
    const hasDescription = project.description != null
    const item: CVTreeItem = {
      id,
      title: project.name,
      hasDescription
    }
    includedCVItems.value.push(id)
    cvItemDescriptionMap.value[id] = hasDescription
    return item
  })

  const groupedSkills = Object.groupBy(profile.skills, (s) => s.type)
  cvItems.value[3].children = Object.keys(groupedSkills).map((skillType) => {
    const groupId = `skillGroup-${skillType}`
    const groupItem: CVTreeItem = {
      id: groupId,
      title: skillType,
      children: groupedSkills[skillType]!.map((skill) => {
        const id = `skill_${skill.id}`
        const skillItem: CVTreeItem = {
          id,
          title: skill.name
        }
        includedCVItems.value.push(id)
        return skillItem
      })
    }
    includedCVItems.value.push(groupId)
    return groupItem
  })
} catch (e) {
  const error = (e as RestError).errorDto
  const errorDetails = error?.message || t('error.genericMessage')
  toastService.error(t('profile.loadingError.title', errorDetails))
}

function onStyleSelected(style: string) {
  selectedStyle.value = style
  cvTemplateOptions.value = {}
}

function toggleCustomizeContent() {
  customizeCVContent.value = !customizeCVContent.value
}

function toggleCustomizeTemplate() {
  customizeCVTemplate.value = !customizeCVTemplate.value
}

function cvStyleClasses(cvStyle: string): ComputedRef {
  return computed(() => {
    return {
      'cv-style-selected': selectedStyle.value === cvStyle,
      'cv-style': true
    }
  })
}

function getStyleImage(cvStyle: string): string {
  return cvStyleImages[cvStyle]
}

function createIncludedCVItem(parts: Array<string>): IncludedCVItem {
  return {
    id: Number.parseInt(parts[1]),
    includeDescription: cvItemDescriptionMap.value[parts[0] + '_' + parts[1]]
  }
}

async function generateCV() {
  if (!selectedStyle.value) {
    return
  }

  if (customizeCVContent.value && includedCVItems.value.length === 0) {
    toastService.error(t('cv.noItemsSelected'))
    return
  }

  isGenerating.value = true

  const selectedItems = customizeCVContent.value
    ? includedCVItems.value.map((id) => id.split('_')).filter((parts) => parts.length === 2)
    : undefined
  const selectedWorkExperiences = selectedItems
    ?.filter((parts) => parts[0] === 'work')
    .map(createIncludedCVItem)
  const selectedEducation = selectedItems
    ?.filter((parts) => parts[0] === 'edu')
    .map(createIncludedCVItem)
  const selectedProjects = selectedItems
    ?.filter((parts) => parts[0] === 'proj')
    .map(createIncludedCVItem)
  const selectedSkills = selectedItems
    ?.filter((parts) => parts[0] === 'skill')
    .map(createIncludedCVItem)
  const templateOptions = customizeCVTemplate.value ? cvTemplateOptions.value : undefined

  try {
    const generationRequest: CVGenerationRequestDto = {
      includedWorkExperience: selectedWorkExperiences,
      includedEducation: selectedEducation,
      includedProjects: selectedProjects,
      includedSkills: selectedSkills,
      templateOptions
    }
    const data = await CVApi.getCV(selectedStyle.value, generationRequest)

    const fileURL = window.URL.createObjectURL(data)
    const a = document.createElement('a')
    a.href = fileURL
    a.download = 'cv.pdf'
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(fileURL)
  } catch (e) {
    const error = (e as RestError).errorDto
    const errorDetails = error?.message || t('error.genericMessage')
    toastService.error(t('cv.generateError', errorDetails))
    return
  } finally {
    isGenerating.value = false
  }
}
</script>

<style scoped lang="scss">
.cv-style.cv-style-selected {
  border: 2px solid rgb(var(--v-theme-primary));
}

.cv-customize-content,
.cv-customize-template{
  align-self: start;
}

.cv-customize-content button.v-btn,
.cv-customize-template button.v-btn {
  margin-bottom: 10px;
}
</style>

<!-- Unscoped styles required since v-html content is not getting scope ids -->
<style lang="scss">
.cv-style div.v-card-text > a {
  text-decoration: underline;
}

.cv-customize-content {
  .cv-item-description-switch .v-input__details {
    display: none;
  }
}
</style>
