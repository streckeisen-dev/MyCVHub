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
              v-bind:form-state="reactive(formState)"
              v-model:error-messages="errorMessages"
            />
            <v-row>
              <form-buttons
                @save="signUp"
                @cancel="cancelSignup"
                :submit-text="t('account.create.submit')"
                cancel-color="error"
              />
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
import { reactive, ref } from 'vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import useVuelidate, { type ValidationArgs } from '@vuelidate/core'
import { type ErrorMessages } from '@/services/FormHelper'
import { useI18n } from 'vue-i18n'
import { email, required } from '@/validation/validators'
import { convertDateToString } from '@/services/DateHelper'
import AccountEditor from '@/views/account/AccountEditor.vue'
import ToastService from '@/services/ToastService'
import LoginStateService from '@/services/LoginStateService'
import { AccountStatus } from '@/dto/AccountStatusDto'
import type { AccountEditorData } from '@/dto/AccountEditorData'
import type { OAuthSignUpRequestDto } from '@/dto/OAuthSignUpRequestDto'
import FormButtons from '@/components/FormButtons.vue'
import AccountApi from '@/api/AccountApi'

if (LoginStateService.getAccountStatus() !== AccountStatus.INCOMPLETE) {
  await router.push({ name: 'account' })
}

const { t } = useI18n({
  useScope: 'global'
})

const errorMessages = ref<ErrorMessages>({})

const formState = reactive<AccountEditorData>({})

const rules = reactive<ValidationArgs>({
  username: { required },
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
  country: { required }
})

const form = useVuelidate<AccountEditorData>(rules, formState)

async function signUp() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const account: OAuthSignUpRequestDto = {
    username: formState.username,
    firstName: formState.firstName,
    lastName: formState.lastName,
    email: formState.email,
    phone: formState.phone,
    birthday: convertDateToString(formState.birthday),
    street: formState.street,
    houseNumber: formState.houseNumber,
    postcode: formState.postcode,
    city: formState.city,
    country: formState.country
  }

  try {
    await accountApi.oauthSignUp(account)
    errorMessages.value = {}
    LoginStateService.setAccountStatus(AccountStatus.UNVERIFIED)
    await router.push({ name: 'account' })
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error?.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      const errorDetails = error?.message || t('error.genericMessage')
      ToastService.error(t('account.signup.error'), errorDetails)
    }
  }
}

async function cancelSignup() {
  try {
    await AccountApi.deleteAccount()
    await AccountApi.logout()
    await router.push({ name: 'home' })
  } catch (e) {
    const error = e as ErrorDto
    const errorDetails = error?.message || t('error.genericMessage')
    ToastService.error(t('account.delete.error'), errorDetails)
  }
}
</script>

<style lang="scss" scoped>
form {
  width: 100%;
}
</style>
