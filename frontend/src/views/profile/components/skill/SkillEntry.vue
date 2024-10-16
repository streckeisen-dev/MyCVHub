<template>
  <v-row class="skill-entry">
    <v-col cols="4" md="2" class="skill-name">{{ skill.name }}</v-col>
    <v-col cols="8" md="10" class="skill-level">
      <v-progress-linear
        :model-value="skill.level"
        color="primary"
        bg-color="btn-background"
        :max="100"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { PublicSkillDto } from '@/dto/PublicSkillDto'
import type { SkillDto } from '@/dto/SkillDto'

const props = defineProps<{
  skill: PublicSkillDto | SkillDto
}>()
</script>

<style lang="scss" scoped>
.skill-entry {
  align-items: center;

  > .skill-name,
  > .skill-level {
    padding-top: 5px;
    padding-bottom: 5px;
  }
}
</style>
