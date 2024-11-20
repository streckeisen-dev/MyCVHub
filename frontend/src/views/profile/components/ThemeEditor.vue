<template>
  <v-row>
    <v-sheet
      color="background"
      class="theme-sheet"
    >
      <v-row class="theme-editor-container">
        <v-col cols="12">
          <v-col
            cols="12"
            md="6"
          >
            <div class="theme-description">
              {{ t('theme.description') }}
            </div>
          </v-col>
        </v-col>
        <v-col
          cols="12"
          sm="6"
          lg="4"
          xl="3"
          xxl="2"
        >
          <v-card
            :title="t('fields.backgroundColor')"
            color="background"
            variant="flat"
          >
            <v-color-picker
              v-model="backgroundColor"
              mode="hex"
              :modes="['hex']"
            />
          </v-card>
        </v-col>
        <v-col
          cols="12"
          sm="6"
          lg="4"
          xl="3"
          xxl="2"
        >
          <v-card
            :title="t('fields.surfaceColor')"
            color="background"
            variant="flat"
          >
            <v-color-picker
              v-model="surfaceColor"
              mode="hex"
              :modes="['hex']"
            />
          </v-card>
        </v-col>
        <v-spacer />
        <v-col
          cols="12"
          lg="6"
        >
          <v-card
            :title="t('theme.preview')"
            color="background"
            flat
          >
            <v-container :style="previewContainerStyles">
              <v-toolbar :style="previewToolbarStyles">
                <v-toolbar-title>{{ t('fields.surfaceColor') }}</v-toolbar-title>
              </v-toolbar>
              <div class="preview-content">{{ t('fields.backgroundColor') }}</div>
            </v-container>
          </v-card>
        </v-col>
        <v-col cols="12">
          <v-row class="action-buttons">
            <v-col
              cols="4"
              sm="3"
              md="2"
              xl="1"
            >
              <v-btn
                :text="t('forms.save')"
                @click="save"
                color="primary"
                :loading="isSaving"
              />
            </v-col>
            <v-col
              cols="12"
              sm="6"
              md="4"
              xl="2"
              xxl="1"
            >
              <v-btn
                :text="t('theme.reset')"
                @click="reset"
                color="background"
                variant="flat"
                :loading="isResetting"
              />
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </v-sheet>
  </v-row>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { computed, ref } from 'vue'
import type { ProfileThemeDto } from '@/dto/ProfileThemeDto'
import vuetify from '@/plugins/vuetify'
import type { ProfileThemeUpdateDto } from '@/dto/ProfileThemeUpdateDto'
import ProfileApi from '@/api/ProfileApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import type { ErrorMessages } from '@/services/FormHelper'
import ToastService from '@/services/ToastService'

const theme = defineModel<ProfileThemeDto | undefined>()

const { t } = useI18n({
  useScope: 'global'
})

const defaultColors = vuetify.theme.themes.value.profile.colors

const backgroundColor = ref<string>(theme.value?.backgroundColor || defaultColors.background)
const surfaceColor = ref<string>(theme.value?.surfaceColor || defaultColors.surface)
const errorMessages = ref<ErrorMessages>({})
const isSaving = ref(false)
const isResetting = ref(false)

const previewContainerStyles = computed(() => {
  return {
    background: backgroundColor.value,
    color: getTextColor(backgroundColor.value),
    padding: 0
  }
})

const previewToolbarStyles = computed(() => {
  return {
    background: surfaceColor.value,
    color: getTextColor(surfaceColor.value)
  }
})

/*
 * used to simulate the automatic determination of text color usually done be vuetify
 * since the text color on non-profile page is determined based on the browser theme, that doesn't work for the preview
 */
function getTextColor(backgroundColor: string) {
  const color = backgroundColor.replace('#', '')
  const r = parseInt(color.slice(0, 2), 16)
  const g = parseInt(color.slice(2, 4), 16)
  const b = parseInt(color.slice(4, 6), 16)
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 155 ? '#000000' : '#FFFFFF'
}

async function save() {
  const themeUpdate: ProfileThemeUpdateDto = {
    backgroundColor: backgroundColor.value,
    surfaceColor: surfaceColor.value
  }

  isSaving.value = true
  try {
    const updatedTheme = await ProfileApi.saveTheme(themeUpdate)
    backgroundColor.value = updatedTheme.backgroundColor
    surfaceColor.value = updatedTheme.surfaceColor

    errorMessages.value = {}
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error?.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      const errorDetails = error?.message || t('error.genericMessage')
      ToastService.error(t('theme.saveError'), errorDetails)
    }
  } finally {
    isSaving.value = false
  }
}

async function reset() {
  backgroundColor.value = defaultColors.background
  surfaceColor.value = defaultColors.surface
  isResetting.value = true
  try {
    await save()
  } catch (error) {
    // already handled
  } finally {
    isResetting.value = false
  }
}
</script>

<style scoped lang="scss">
.theme-sheet {
  width: 100%;
  padding: 20px;

  .theme-editor-container {
    margin-left: 15px;

    .preview-content {
      padding: 20px;
    }

    .action-buttons {
      display: flex;
      gap: 10px;
    }
  }
}
</style>

<style lang="scss">
.theme-description {
  white-space: break-spaces;
  ul {
    margin-top: -45px;
    margin-bottom: -45px;
    padding-left: 20px;

    li {
      margin-bottom: -25px;
    }
  }

  br {
    margin: 0;
  }
}
</style>
