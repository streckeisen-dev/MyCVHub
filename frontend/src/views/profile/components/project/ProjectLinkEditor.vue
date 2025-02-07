<template>
  <v-col cols="12">
    <v-row
      v-for="(link, index) in model"
      :key="index"
    >
      <v-col cols="8">
        <v-text-field
          v-model="link.url"
          :label="t('fields.url')"
          outlined
          :error-messages="getURLErrors(index).value"
        />
      </v-col>
      <v-col cols="2">
        <v-select
          v-model="link.type"
          :items="Object.values(ProjectLinkType)"
          :label="t('fields.type')"
          :error-messages="getTypeErrors(index).value"
        />
      </v-col>
      <v-col cols="1">
        <v-btn
          icon="mdi-minus"
          color="primary"
          @click="removeLink(index)"
        />
      </v-col>
      <v-col
        v-if="index === model.length - 1"
        cols="1"
      >
        <v-btn
          icon="mdi-plus"
          color="primary"
          @click="addLink"
        />
      </v-col>
    </v-row>
    <v-row v-if="model.length === 0">
      <v-col cols="12">
        <v-btn
          icon="mdi-plus"
          color="primary"
          @click="addLink"
        />
      </v-col>
    </v-row>
  </v-col>
</template>

<script setup lang="ts">
import { ProjectLinkUpdateDto } from '@/dto/ProjectLinkUpdateDto'
import { ProjectLinkType } from '@/dto/ProjectLink'
import { useI18n } from 'vue-i18n'
import { ErrorMessages, getIndexedErrorMessages } from '@/services/FormHelper.ts'
import { computed, ComputedRef, Ref } from 'vue'

const { t } = useI18n({
  useScope: 'global'
})

const model = defineModel<Array<ProjectLinkUpdateDto>>({
  required: true
})

const props = defineProps<{
  errorMessages: Ref<ErrorMessages>
}>()

function getErrors(index: number): ComputedRef<ErrorMessages> {
  return getIndexedErrorMessages(props.errorMessages, 'links', index)
}

function getURLErrors(index: number): ComputedRef {
  return computed(() => {
    return getErrors(index).value.url
  })
}

function getTypeErrors(index: number): ComputedRef {
  return computed(() => {
    return getErrors(index).value.type
  })
}

function addLink() {
  model.value.push({
    type: undefined,
    url: undefined
  })
}

function removeLink(index: number) {
  model.value.splice(index, 1)
}
</script>
