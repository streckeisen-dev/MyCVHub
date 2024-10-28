<template>
  <div>
    <template v-if="details">
      <h4>{{ message }}</h4>
      <p>{{ details }}</p>
    </template>
    <p v-else>{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  message: string
  details?: string
}>()
</script>

<style scoped lang="scss"></style>
