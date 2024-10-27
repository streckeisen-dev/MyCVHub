<template>
  <v-main>
    <v-container v-if="account">
      <v-row>
        <v-col cols="12">
          <h1>{{ t('account.edit.title') }}</h1>
        </v-col>
      </v-row>
      <v-form>
        <account-editor
          v-model:form="form"
          v-model:form-state="formState"
          v-model:error-messages="errorMessages"
        />

        <form-buttons :is-saving="isSaving" @save="save" @cancel="cancel" />
      </v-form>
    </v-container>
    <v-container v-else-if="isLoading">
      <loading-spinner />
    </v-container>
    <v-empty-state
      v-else
      :headline="t('error.genericMessage')"
      :title="t('account.loadingError.title')"
      :text="t('account.loadingError.text')"
    />
  </v-main>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AccountEditor from '@/views/account/AccountEditor.vue'
import { reactive, ref } from 'vue'
import { email, required } from '@/validation/validators'
import useVuelidate from '@vuelidate/core'
import type { ErrorMessages } from '@/services/FormHelper'
import router from '@/router'
import type { AccountEditorData } from '@/dto/AccountEditorData'
import type { AccountUpdateDto } from '@/dto/AccountUpdateDto'
import FormButtons from '@/components/FormButtons.vue'
import type { AccountDto } from '@/dto/AccountDto'
import accountApi from '@/api/AccountApi'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import type { ErrorDto } from '@/dto/ErrorDto'

const { t } = useI18n()

const isLoading = ref(true)
const isSaving = ref(false)
const account = ref<AccountDto>()
const errorMessages = ref<ErrorMessages>({})

try {
  account.value = await accountApi.getAccountInfo()
} catch (ignore) {
  // ignore
} finally {
  isLoading.value = false
}

const formState = reactive<AccountEditorData>({
  firstName: account.value?.firstName,
  lastName: account.value?.lastName,
  email: account.value?.email,
  phone: account.value?.phone,
  birthday: convertStringToDate(account.value?.birthday),
  street: account.value?.street,
  houseNumber: account.value?.houseNumber,
  postcode: account.value?.postcode,
  city: account.value?.city,
  country: account.value?.country
})

const rules = {
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
}

const form = useVuelidate<AccountEditorData>(rules, formState)

async function save() {
  const isValid = form.value.$validate()
  if (!isValid) {
    return
  }

  const accountUpdate: AccountUpdateDto = {
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

  isSaving.value = true
  try {
    await accountApi.update(accountUpdate)
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
