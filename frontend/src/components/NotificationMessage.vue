<template>
	<v-snackbar
		v-model="shouldShowSnackbar"
		location="top right"
		multi-line>
		<h3>{{ title }}</h3>
		<p>{{ message }}</p>
		<template v-slot:actions>
			<v-btn
				icon="mdi-close"
				@click="shouldShowSnackbar = false" />
		</template>
	</v-snackbar>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
	title: string
	message: string
}>()
const shouldShowSnackbar = ref<boolean>(true)
const title = ref<string>(props.title)
const message = ref<string>(props.message)
</script>

<style scoped></style>
