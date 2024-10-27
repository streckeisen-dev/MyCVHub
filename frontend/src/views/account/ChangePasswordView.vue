<template>
	<v-main>
		<v-container>
			<v-row>
				<v-col>
					<h1>{{ t('account.edit.changePassword') }}</h1>
				</v-col>
			</v-row>
			<v-row>
				<v-col
					cols="12"
					md="6"
				>
					<v-form>
						<password-input
							v-model="formState.oldPassword"
							:label="t('fields.oldPassword')"
							:error-messages="oldPasswordErrors"
						/>
						<password-input
							v-model="formState.password"
							:label="t('fields.password')"
							:error-messages="passwordErrors"
						/>
						<password-input
							v-model="formState.confirmPassword"
							:label="t('fields.confirmPassword')"
							:error-messages="confirmPasswordErrors"
						/>

						<form-buttons
							@save="save"
							@cancel="cancel"
							:is-saving="isSaving"
						/>
					</v-form>
				</v-col>
				<v-col
					cols="12"
					md="6"
				>
					<password-requirements
						v-model:form-state="formState"
						v-model:rules="rules"
					/>
				</v-col>
			</v-row>
		</v-container>
	</v-main>
</template>

<script setup lang="ts">
import PasswordInput from '@/components/PasswordInput.vue'
import { useI18n } from 'vue-i18n'
import { type ComputedRef, reactive, ref } from 'vue'
import { required } from '@/validation/validators'
import PasswordRequirements from '@/views/account/PasswordRequirements.vue'
import useVuelidate, { type ValidationArgs } from '@vuelidate/core'
import FormButtons from '@/components/FormButtons.vue'
import router from '@/router'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ChangePasswordRequestDto } from '@/dto/ChangePasswordRequestDto'
import accountApi from '@/api/AccountApi'
import type { ErrorDto } from '@/dto/ErrorDto'

const { t } = useI18n()

const errorMessages = ref<ErrorMessages>({})
const isSaving = ref(false)

type FormState = {
	oldPassword?: string
	password?: string
	confirmPassword?: string
}

const formState = reactive<FormState>({
	oldPassword: undefined,
	password: undefined,
	confirmPassword: undefined
})

const rules = reactive<ValidationArgs>({
	oldPassword: {
		required
	},
	password: {
		required
	},
	confirmPassword: {
		required
	}
})

const form = useVuelidate<FormState>(rules, formState)

function getErrors(attributeName: string): ComputedRef {
	return getErrorMessages(errorMessages, form, attributeName)
}

const oldPasswordErrors = getErrors('oldPassword')
const passwordErrors = getErrors('password')
const confirmPasswordErrors = getErrors('confirmPassword')

async function save() {
	const isValid = await form.value.$validate()
	if (!isValid) {
		return
	}

	const changePasswordRequest: ChangePasswordRequestDto = {
		oldPassword: formState.oldPassword,
		password: formState.password,
		confirmPassword: formState.confirmPassword
	}

	isSaving.value = true
	try {
		await accountApi.changePassword(changePasswordRequest)
		await router.push({ name: 'account' })
	} catch (e) {
		const error = e as ErrorDto
		errorMessages.value = error.errors || {}
	} finally {
		isSaving.value = false
	}
}

async function cancel() {
	await router.push({ name: 'account' })
}
</script>

<style scoped lang="scss"></style>
