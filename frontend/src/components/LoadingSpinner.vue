<template>
  <v-progress-circular
    indeterminate
    color="secondary"
    class="loading-spinner"
  />
</template>

<style scoped>
.loading-spinner {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
</style>
