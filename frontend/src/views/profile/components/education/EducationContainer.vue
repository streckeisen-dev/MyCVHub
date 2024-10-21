<template>
  <v-row
    v-for="(education, index) in sortedEducation"
    :key="(education as EducationDto).id || index"
    class="education"
    justify="end"
  >
    <v-col cols="12" :md="actions ? 10 : 12">
      <education-entry :education="education" />
    </v-col>
    <template v-if="actions">
      <v-col cols="3" sm="2" md="1" class="education-action">
        <v-btn icon="mdi-pencil" color="primary" @click="editEducation(education as EducationDto)" />
      </v-col>
      <v-col cols="3" sm="2" md="1" class="education-action">
        <v-btn icon="mdi-delete" color="red" @click="deleteEducation((education as EducationDto).id)" />
      </v-col>
    </template>
  </v-row>
  <v-col v-if="values.length === 0" cols="12">{{ t('education.noEntries') }}</v-col>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { compareDatesByYearAndMonth, convertStringToDate } from '@/services/DateHelper'
import type { PublicEducationDto } from '@/dto/PublicEducationDto'
import type { EducationDto } from '@/dto/EducationDto'
import EducationEntry from '@/views/profile/components/education/EducationEntry.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  values: Array<EducationDto> | Array<PublicEducationDto>
  actions?: boolean
}>()

const emit = defineEmits(['edit', 'delete'])

const sortedEducation = computed(() => {
  return props.values.sort((a, b) => {
    const educationEndA = convertStringToDate(a.educationEnd)
    const educationEndB = convertStringToDate(b.educationEnd)
    const educationStartA = convertStringToDate(a.educationStart)!!
    const educationStartB = convertStringToDate(b.educationStart)!!

    const educationEndComparison = compareDatesByYearAndMonth(educationEndA, educationEndB)
    if (educationEndComparison === 0) {
      return compareDatesByYearAndMonth(educationStartA, educationStartB)
    }
    return educationEndComparison
  })
})

function editEducation(education: EducationDto) {
  emit('edit', education)
}

function deleteEducation(id: number) {
  emit('delete', id)
}
</script>

<i18n>
{
  "de": {
    "education": {
      "noEntries": "Noch keine Ausbildungen."
    }
  },
  "en": {
    "education": {
      "noEntries": "No education entries yet."
    }
  }
}
</i18n>

<style lang="scss" scoped>
.education {
  margin: 10px 0 0 0;

  .education-action {
    align-self: center;
  }
}
</style>
