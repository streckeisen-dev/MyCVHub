<template>
  <v-btn :href="link.url" target="_blank" :icon="mdiIcon" variant="text" />
</template>

<script setup lang="ts">
import { ProjectLink, ProjectLinkType } from '@/dto/ProjectLink'
import { computed } from 'vue'

const props = defineProps<{
  link: ProjectLink
}>()

const mdiIcon = computed(() => {
  switch (props.link.type) {
    case ProjectLinkType.GITHUB:
      return 'mdi-github'
    case ProjectLinkType.DOCUMENT:
      return 'mdi-file'
    case ProjectLinkType.WEBSITE:
      return 'mdi-web'
    default:
      return 'mdi-link'
  }
})
</script>
