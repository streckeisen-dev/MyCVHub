<template>
  <v-main>
    <v-container>
      <v-row>
        <v-col cols="12">
          <h2>{{ t('cv.generate') }}</h2>
          <p>{{ t('cv.intro') }}</p>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12">
          <h3>{{ t('cv.style') }}</h3>
          <p>{{ t('cv.styleExplanation') }}</p>
        </v-col>
        <v-col
          cols="6"
          md="4"
          xl="2"
        >
          <v-card
            v-for="style in cvStyles"
            :key="style.key"
            elevation="5"
            rounded
            variant="outlined"
            :class="cvStyleClasses(style.key).value"
          >
            <v-img
              :src="getStyleImage(style.key)"
            />
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
      </v-row>
      <v-row>
        <v-col cols="12">
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

const { t } = useI18n({
  useScope: 'global'
})

const selectedStyle = ref<string>()
const cvStyles = ref<Array<CVStyleDto>>([])

const cvStyleImagesGlob = import.meta.glob<any>('@/assets/cv_styles/*.jpg', { eager: true })
const cvStyleImages = Object.fromEntries(Object.entries(cvStyleImagesGlob).map(([key, value]) => [filename(key), value.default]))

const isGenerating = ref(false)

try {
  cvStyles.value = await CVApi.getCVStyles()
} catch (e) {
  const error = (e as RestError).errorDto
  const errorDetails = error?.message || t('error.genericMessage')
  toastService.error(t('cv.styleError', errorDetails))
}

function onStyleSelected(style: string) {
  selectedStyle.value = style
}

async function generateCV() {
  if (!selectedStyle.value) {
    return
  }

  try {
    isGenerating.value = true
    const data = await CVApi.getCV(selectedStyle.value)
    const fileURL = window.URL.createObjectURL(data)
    const a = document.createElement('a')
    a.href = fileURL
    a.download = 'cv.pdf'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
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
</script>

<style scoped lang="scss">
.cv-style.cv-style-selected {
  border-color: rgb(var(--v-theme-primary));
}
</style>

<!-- Unscoped styles required since v-html content is not getting scope ids -->
<style lang="scss">
.cv-style div.v-card-text > a {
  text-decoration: underline;
}
</style>