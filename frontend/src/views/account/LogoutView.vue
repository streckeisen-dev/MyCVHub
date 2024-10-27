<template>
	<v-main>
		<v-empty-state
			:headline="t('account.logout.headline')"
			:title="t('account.logout.action')"
			:text="t('account.logout.message')" />
		<notification
			v-if="logoutError"
			:title="t('account.logout.errorTitle')"
			:message="t('account.logout.errorMessage')" />
	</v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import Notification from '@/components/NotificationMessage.vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
	useScope: 'global'
})

const logoutError = ref(false)

if (accountApi.isUserLoggedIn()) {
	try {
		await accountApi.logout()
		setTimeout(async () => {
			await router.push({ name: 'home' })
		}, 2000)
	} catch (error) {
		setTimeout(() => {
			router.back()
		}, 2000)
	}
} else {
	await router.push({ name: 'home' })
}
</script>
