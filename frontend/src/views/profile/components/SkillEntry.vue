<template>
  <v-row class="skill-entry">
    <v-col cols="2" class="skill-name">{{ skill.name }}</v-col>
    <v-col cols="10" class="skill-level">
      <v-progress-linear :model-value="skill.level" />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { PublicSkillDto } from '@/dto/PublicSkillDto'

const props = defineProps<{
  skill: PublicSkillDto
}>()
</script>

<style lang="scss" scoped>
.skill-entry {
  align-items: center;

  > .skill-name, > .skill-level {
    padding-top: 5px;
    padding-bottom: 5px;
  }
}
</style>