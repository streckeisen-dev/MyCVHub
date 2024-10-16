<template>
  <v-row class="skills-container">
    <v-col cols="12">
      <v-row v-for="(skills, skillType) in groupedSkills" :key="skillType">
        <v-col cols="12" class="skill-type">
          <h3>{{ skillType }}</h3>
        </v-col>
        <v-col cols="12" class="skills">
          <v-row v-for="(skill, index) in sortSkills(skills)" :key="(skill as SkillDto).id || index" class="skill" justify="end">
            <v-col cols="12" :md="actions ? 10 : 12">
              <skill-entry :skill="skill" />
            </v-col>
            <template v-if="actions">
              <v-col cols="3" sm="2" md="1" class="skill-action">
                <v-btn icon="mdi-pencil" color="primary" @click="editSkill(skill as SkillDto)" />
              </v-col>
              <v-col cols="3" sm="2" md="1" class="skill-action">
                <v-btn icon="mdi-delete" color="red" @click="deleteSkill((skill as SkillDto).id)" />
              </v-col>
            </template>
          </v-row>
        </v-col>
      </v-row>
      <template v-if="values.length === 0">No skills yet.</template>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { SkillDto } from '@/dto/SkillDto'
import { computed } from 'vue'
import SkillEntry from '@/views/profile/components/skill/SkillEntry.vue'
import type { PublicSkillDto } from '@/dto/PublicSkillDto'

const props = defineProps<{
  values: Array<SkillDto> | Array<PublicSkillDto>
  actions?: boolean
}>()

const emit = defineEmits(['edit', 'delete'])

const groupedSkills = computed(() => {
  return props.values.reduce((groups: { [key: string]: Array<SkillDto | PublicSkillDto> }, skill: SkillDto | PublicSkillDto) => {
    const group = groups[skill.type] || []
    group.push(skill)
    groups[skill.type] = group
    return groups
  }, {})
})

function sortSkills(
  skills: Array<SkillDto | PublicSkillDto>
): Array<SkillDto | PublicSkillDto> {
  return skills.sort((a, b) => {
    if (a.level > b.level) {
      return -1
    } else if (a.level < b.level) {
      return 1
    } else {
      return 0
    }
  })
}

function editSkill(skill: SkillDto) {
  emit('edit', skill)
}

function deleteSkill(id: number) {
  emit('delete', id)
}
</script>

<style lang="scss" scoped>
.skills-container {
  margin: 10px 0 0 0;

  .skill-type {
    padding-bottom: 0;
  }

  .skills {
    padding-top: 0;

    .skill {
      margin: 10px 0 0 0;
      align-items: center;

      @media screen and (min-width: 980px) {
        .skill-entry {
          padding-right: 20px;
        }
      }
    }
  }
}
</style>
