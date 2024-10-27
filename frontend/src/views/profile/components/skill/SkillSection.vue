<template>
  <div class="skill-section">
    <h3>{{ skillType }}</h3>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  skillType: string
}>()
</script>

<style scoped>
h3,
.skill-section {
  margin-bottom: 10px;
}
</style>
