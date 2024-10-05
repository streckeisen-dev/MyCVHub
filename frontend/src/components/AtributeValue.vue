<template>
  <dt>{{ name }}</dt>
  <dd>{{ value }}</dd>
</template>

<script setup lang="ts">
const props = defineProps<{
  name: string
  value: string
}>()
</script>

<style scoped>
dt {
  font-weight: bold;
}
</style>
