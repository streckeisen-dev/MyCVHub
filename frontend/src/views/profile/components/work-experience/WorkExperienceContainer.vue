<template>
  <v-row
    v-for="workExperience in sortedWorkExperiences"
    :key="workExperience.id"
    class="work-experiences"
    justify="end"
  >
    <v-col cols="12" :md="actions ? 10 : 12">
      <work-experience-entry :work-experience="workExperience" />
    </v-col>
    <template v-if="actions">
      <v-col cols="3" sm="2" md="1" class="work-experience-action">
        <v-btn icon="mdi-pencil" color="btn-primary" @click="editWorkExperience(workExperience)" />
      </v-col>
      <v-col cols="3" sm="2" md="1" class="work-experience-action">
        <v-btn icon="mdi-delete" color="red" @click="deleteWorkExperience(workExperience.id)" />
      </v-col>
    </template>
  </v-row>
  <v-col v-if="values.length === 0" cols="12">No work experiences yet.</v-col>
</template>

<script setup lang="ts">
import WorkExperienceEntry from '@/views/profile/components/work-experience/WorkExperienceEntry.vue'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import type { PublicWorkExperienceDto } from '@/dto/PublicWorkExperienceDto'
import { computed } from 'vue'
import { compareDatesByYearAndMonth, convertStringToDate } from '@/services/DateHelper'

const props = defineProps<{
  values: Array<WorkExperienceDto> | Array<PublicWorkExperienceDto>
  actions?: boolean
}>()

const emit = defineEmits(['edit', 'delete'])

const sortedWorkExperiences = computed(() => {
  return props.values.sort((a, b) => {
    const positionEndA = convertStringToDate(a.positionEnd)
    const positionEndB = convertStringToDate(b.positionEnd)
    const positionStartA = convertStringToDate(a.positionStart)!!
    const positionStartB = convertStringToDate(b.positionStart)!!

    const positionEndComparison = compareDatesByYearAndMonth(positionEndA, positionEndB)
    if (positionEndComparison === 0) {
      return compareDatesByYearAndMonth(positionStartA, positionStartB)
    }
    return positionEndComparison
  })
})

function editWorkExperience(workExperience: WorkExperienceDto) {
  emit('edit', workExperience)
}

function deleteWorkExperience(id: number) {
  emit('delete', id)
}
</script>

<style lang="scss" scoped>
.work-experiences {
  margin: 10px 0 0 0;

  .work-experience-action {
    align-self: center;
  }
}
</style>
