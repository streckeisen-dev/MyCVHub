<template>
	<v-main>
		<v-empty-state
			:headline="t('notFound.headline')"
			:title="t('notFound.title')"
			:text="t('notFound.text')"
		/>
	</v-main>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
	useScope: 'global'
})
</script>
