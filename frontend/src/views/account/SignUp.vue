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
            <v-row>
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h2>{{ t('account.personalData') }}</h2>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.firstName"
                      :label="t('fields.firstName')"
                      :error-messages="firstNameErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.lastName"
                      :label="t('fields.lastName')"
                      :error-messages="lastNameErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.email"
                      :label="t('fields.email')"
                      :error-messages="emailErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.phone"
                      :label="t('fields.phone')"
                      :error-messages="phoneErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-date-input
                      v-model="formState.birthday"
                      :label="t('fields.birthday')"
                      :error-messages="birthdayErrors"
                    />
                  </v-col>
                </v-row>
              </v-col>
              <v-spacer />
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h2>{{ t('account.address') }}</h2>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12" sm="9">
                    <v-text-field
                      v-model="formState.street"
                      :label="t('fields.street')"
                      :error-messages="streetErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="3">
                    <v-text-field
                      v-model="formState.houseNumber"
                      :label="t('fields.houseNumber')"
                      :error-messages="houseNumberErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="3">
                    <v-text-field
                      v-model="formState.postcode"
                      :label="t('fields.postcode')"
                      :error-messages="postcodeErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="9">
                    <v-text-field
                      v-model="formState.city"
                      :label="t('fields.city')"
                      :error-messages="cityErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="12">
                    <v-autocomplete
                      v-model="formState.country"
                      :label="t('fields.country')"
                      :items="countries"
                      item-title="name"
                      item-value="countryCode"
                      :error-messages="countryErrors"
                    />
                  </v-col>
                </v-row>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" md="6">
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
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h3>{{ t('account.passwordRequirements') }}</h3>
                  </v-col>
                </v-row>
                <v-row>
                  <v-list density="comfortable">
                    <v-list-item
                      v-for="requirement in passwordRequirements"
                      :key="requirement.name"
                      :title="requirement.name"
                      :class="
                        requirement.predicate()
                          ? 'pw-requirement-fulfilled'
                          : 'pw-requirement-error'
                      "
                    />
                  </v-list>
                </v-row>
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
      <notification
        v-if="didCountryLoadFail"
        :title="t('country.loadingError.title')"
        :message="t('country.loadingError.message')"
      />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { VDateInput } from 'vuetify/labs/components'
import { computed, type ComputedRef, reactive, ref, watch } from 'vue'
import PasswordInput from '@/components/PasswordInput.vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import useVuelidate from '@vuelidate/core'
import type { CountryDto } from '@/dto/CountryDto'
import countryApi from '@/api/CountryApi'
import Notification from '@/components/Notification.vue'
import { getErrorMessages } from '@/services/FormHelper'
import { useI18n } from 'vue-i18n'
import { email, required, withI18nMessage } from '@/validation/validators'
import type { SignupRequestDto } from '@/dto/SignUpRequestDto'
import { convertDateToString } from '@/services/DateHelper'
import { useLocale } from 'vuetify'

if (accountApi.isUserLoggedIn()) {
  await router.push({ name: 'home' })
}

const { t } = useI18n({
  useScope: 'global'
})

const countries = ref<Array<CountryDto>>([])
const errorMessages = ref<{ [key: string]: string }>({})
const didCountryLoadFail = ref<boolean>(false)

async function loadCountries() {
  try {
    countries.value = await countryApi.getCountries()
  } catch (error) {
    didCountryLoadFail.value = true
  }
}
await loadCountries()

watch(useLocale().current, async () => {
  didCountryLoadFail.value = false
  await loadCountries()
})

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

const passwordRequirements = computed(() => [
  {
    name: t('passwordRequirements.length', { length: '8' }),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.length >= 8
    }
  },
  {
    name: t('passwordRequirements.whitespaces'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw != '' && !pw.includes(' ')
    }
  },
  {
    name: t('passwordRequirements.digits'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return /\d/.test(pw)
    }
  },
  {
    name: t('passwordRequirements.specialChars'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return /\W/.test(pw)
    }
  },
  {
    name: t('passwordRequirements.uppercase'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.toLowerCase() !== pw
    }
  },
  {
    name: t('passwordRequirements.lowercase'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.toUpperCase() !== pw
    }
  },
  {
    name: t('passwordRequirements.match'),
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw === formState.confirmPassword
    }
  }
])
const passwordValidator = withI18nMessage(() =>
  passwordRequirements.value.every((r) => r.predicate())
)

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
  country: { required },
  password: {
    required,
    passwordValidator
  },
  confirmPassword: {
    required
  }
}

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
    password: formState.password
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

const firstNameErrors = getErrors('firstName')
const lastNameErrors = getErrors('lastName')
const emailErrors = getErrors('email')
const phoneErrors = getErrors('phone')
const birthdayErrors = getErrors('birthday')
const streetErrors = getErrors('street')
const houseNumberErrors = getErrors('houseNumber')
const postcodeErrors = getErrors('postcode')
const cityErrors = getErrors('city')
const countryErrors = getErrors('country')
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
