<template>
  <v-col class="profile-section">
    <h2 v-if="h2">{{ title }}</h2>
    <h3 v-else>{{ title }}</h3>
    <slot />
  </v-col>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  h2?: boolean
}>()
</script>
