<template>
  <v-color-input v-if="isColor" v-model="model" :label="option.name" />
  <v-text-field v-else-if="isString" v-model="model" :label="option.name" />
  <p v-else>ERROR</p>
</template>

<script setup lang="ts">

import { CVStyleOptionDto } from '@/dto/CVStyleDto.ts'
import { computed } from 'vue'
import { CVStyleOptionType } from '@/dto/CVStyleOptionType.ts'
import { VColorInput } from 'vuetify/labs/VColorInput'

const props = defineProps<{
  option: CVStyleOptionDto
}>()

const model = defineModel<string | undefined>({
  required: true,
})
model.value = props.option.default

const isColor = computed(() => props.option.type === CVStyleOptionType.COLOR)
const isString = computed(() => props.option.type === CVStyleOptionType.STRING)
</script>
