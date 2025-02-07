<template>
  <v-text-field
    v-model="model"
    :label="props.label"
    :type="showPassword ? 'text' : 'password'"
    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
    @click:append="showPassword = !showPassword"
    :error-messages="errorMessages"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const model = defineModel<string>()

const props = defineProps<{
  label: string
  errorMessages?: [string] | string | undefined
}>()

const showPassword = ref<boolean>(false)
</script>
