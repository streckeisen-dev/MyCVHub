<template>
	<v-main>
		<v-container>
			<v-row>
				<v-col cols="12">
					<h1>{{ t('account.create.title') }}</h1>
				</v-col>
			</v-row>
			<v-row>
				<v-form>
					<v-container>
						<account-editor
							v-model:form="form"
							v-model:form-state="formState"
							v-model:error-messages="errorMessages"
						/>
						<v-row>
							<v-col
								cols="12"
								md="6"
							>
								<v-row>
									<v-col cols="12">
										<h2>{{ t('fields.password') }}</h2>
									</v-col>
								</v-row>
								<v-row>
									<v-col cols="12">
										<password-input
											v-model="formState.password"
											:label="t('fields.password')"
											:error-messages="passwordErrors"
										/>
									</v-col>
									<v-col cols="12">
										<password-input
											v-model="formState.confirmPassword"
											:label="t('fields.confirmPassword')"
											:error-messages="confirmPasswordErrors"
										/>
									</v-col>
								</v-row>
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
						<v-row>
							<v-col cols="12">
								<v-btn
									color="primary"
									type="submit"
									@click.prevent="signUp"
									:text="t('account.create.submit')"
								/>
							</v-col>
						</v-row>
					</v-container>
				</v-form>
			</v-row>
		</v-container>
	</v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { type ComputedRef, reactive, ref } from 'vue'
import PasswordInput from '@/components/PasswordInput.vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import useVuelidate, { type ValidationArgs } from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import { useI18n } from 'vue-i18n'
import { email, required } from '@/validation/validators'
import type { SignupRequestDto } from '@/dto/SignUpRequestDto'
import { convertDateToString } from '@/services/DateHelper'
import AccountEditor from '@/views/account/AccountEditor.vue'
import PasswordRequirements from '@/views/account/PasswordRequirements.vue'

if (accountApi.isUserLoggedIn()) {
	await router.push({ name: 'home' })
}

const { t } = useI18n({
	useScope: 'global'
})

const errorMessages = ref<ErrorMessages>({})

type FormState = {
	firstName?: string
	lastName?: string
	email?: string
	phone?: string
	birthday?: Date
	street?: string
	houseNumber?: string
	postcode?: string
	city?: string
	country?: string
	password?: string
	confirmPassword?: string
}

const formState = reactive<FormState>({
	firstName: undefined,
	lastName: undefined,
	email: undefined,
	phone: undefined,
	birthday: undefined,
	street: undefined,
	houseNumber: undefined,
	postcode: undefined,
	city: undefined,
	country: undefined,
	password: undefined,
	confirmPassword: undefined
})

const rules = reactive<ValidationArgs>({
	firstName: { required },
	lastName: { required },
	email: {
		required,
		email
	},
	phone: { required },
	birthday: { required },
	street: { required },
	houseNumber: {},
	postcode: { required },
	city: { required },
	country: { required },
	password: {
		required
	},
	confirmPassword: {
		required
	}
})

const form = useVuelidate<FormState>(rules, formState)

async function signUp() {
	const isValid = await form.value.$validate()
	if (!isValid) {
		return
	}

	const account: SignupRequestDto = {
		firstName: formState.firstName,
		lastName: formState.lastName,
		email: formState.email,
		phone: formState.phone,
		birthday: convertDateToString(formState.birthday),
		street: formState.street,
		houseNumber: formState.houseNumber,
		postcode: formState.postcode,
		city: formState.city,
		country: formState.country,
		password: formState.password,
		confirmPassword: formState.confirmPassword
	}

	try {
		await accountApi.signUp(account)
		errorMessages.value = {}
		await router.push({ name: 'account' })
	} catch (e) {
		const error = e as ErrorDto
		errorMessages.value = error.errors || {}
	}
}

function getErrors(attributeName: string): ComputedRef {
	return getErrorMessages(errorMessages, form, attributeName)
}

const passwordErrors = getErrors('password')
const confirmPasswordErrors = getErrors('confirmPassword')
</script>

<style lang="scss" scoped>
form {
	width: 100%;

	div.v-list-item {
		min-height: auto;

		&.pw-requirement-fulfilled {
			color: green;
		}

		&.pw-requirement-error {
			color: red;
		}
	}
}
</style>
