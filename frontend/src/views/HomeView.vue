<template>
  <main>
    <p>REST Connection: {{ restConnection }}</p>
    <TheWelcome />
  </main>
</template>

<script setup lang="ts">
import TheWelcome from '../components/TheWelcome.vue'
import testApi from '@/api/test-api'
import { ref } from 'vue'

const restConnection = ref<String>('untested')
try {
  restConnection.value = (await testApi.testConnection()).message
  console.log(restConnection.value)
} catch (e) {
  console.error('API Test failed', e)
  restConnection.value = 'test failed'
}
</script>