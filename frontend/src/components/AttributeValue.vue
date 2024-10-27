<template>
	<dt>{{ name }}</dt>
	<dd>
		<a
			v-if="link"
			:href="href"
			>{{ value }}</a
		>
		<p v-else>{{ value || '―' }}</p>
	</dd>
</template>

<script setup lang="ts">
defineProps<{
	name: string
	value: string | undefined
	link?: boolean
	href?: string
}>()
</script>

<style scoped>
dt {
	font-weight: bold;
}
</style>
