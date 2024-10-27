<template>
	<dl class="attribute-list">
		<slot />
	</dl>
</template>

<script setup lang="ts"></script>

<style scoped>
.attribute-list {
	display: grid;
	gap: 10px;
	grid-template-columns: auto auto;
}
</style>
