<template>
  <v-color-input v-if="isColor" v-model="model" :label="option.name" />
  <v-text-field v-else-if="isString" v-model="model" :label="option.name" />
  <v-alert v-else
           color="error"
           icon="$error"
           :title="t('error.genericMessageTitle')"
           density="compact"
  />
</template>

<script setup lang="ts">

import { CVStyleOptionDto } from '@/dto/CVStyleDto.ts'
import { computed } from 'vue'
import { CVStyleOptionType } from '@/dto/CVStyleOptionType.ts'
import { VColorInput } from 'vuetify/labs/VColorInput'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  option: CVStyleOptionDto
}>()

const model = defineModel<string | undefined>({
  required: true,
})
model.value = props.option.default

const { t } = useI18n({
  useScope: 'global'
})

const isColor = computed(() => props.option.type === CVStyleOptionType.COLOR)
const isString = computed(() => props.option.type === CVStyleOptionType.STRING)
</script>
