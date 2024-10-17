<template>
  <v-main>
    <v-container>
      <v-row>
        <v-col cols="12">
          <h1>Create an account</h1>
        </v-col>
      </v-row>
      <v-row>
        <v-form>
          <v-container>
            <v-row>
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h2>Personal Data</h2>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.firstName"
                      label="First Name"
                      :error-messages="firstNameErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.lastName"
                      label="Last Name"
                      :error-messages="lastNameErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.email"
                      label="E-Mail"
                      :error-messages="emailErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-text-field
                      v-model="formState.phone"
                      label="Phone"
                      :error-messages="phoneErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <v-date-input
                      v-model="formState.birthday"
                      label="Birthday"
                      :error-messages="birthdayErrors"
                    />
                  </v-col>
                </v-row>
              </v-col>
              <v-spacer />
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h2>Address</h2>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12" sm="9">
                    <v-text-field
                      v-model="formState.street"
                      label="Street"
                      :error-messages="streetErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="3">
                    <v-text-field
                      v-model="formState.houseNumber"
                      label="Number"
                      :error-messages="houseNumberErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="3">
                    <v-text-field
                      v-model="formState.postcode"
                      label="Postcode"
                      :error-messages="postcodeErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="9">
                    <v-text-field
                      v-model="formState.city"
                      label="City"
                      :error-messages="cityErrors"
                    />
                  </v-col>
                  <v-col cols="12" sm="12">
                    <v-autocomplete
                      v-model="formState.country"
                      label="Country"
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
                    <h2>Password</h2>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <password-input
                      v-model="formState.password"
                      label="Password"
                      :error-messages="passwordErrors"
                    />
                  </v-col>
                  <v-col cols="12">
                    <password-input
                      v-model="formState.confirmedPassword"
                      label="Confirm Password"
                      :error-messages="confirmedPasswordErrors"
                    />
                  </v-col>
                </v-row>
              </v-col>
              <v-col cols="12" md="6">
                <v-row>
                  <v-col cols="12">
                    <h3>Password Requirements</h3>
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
                <v-btn color="primary" @click.stop="signUp">Sign Up</v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-form>
      </v-row>
      <notification
        v-if="didCountryLoadFail"
        title="Failed to load countries"
        message="Try again later or contact the administrator if the problem isn't resolved"
      />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { VDateInput } from 'vuetify/labs/components'
import { computed, type ComputedRef, reactive, ref } from 'vue'
import PasswordInput from '@/components/PasswordInput.vue'
import type { AccountDto } from '@/dto/AccountDto'
import type { ErrorDto } from '@/dto/ErrorDto'
import { email, helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import type { CountryDto } from '@/dto/CountryDto'
import countryApi from '@/api/CountryApi'
import Notification from '@/components/Notification.vue'
import { getErrorMessages } from '@/services/FormHelper'

if (accountApi.isUserLoggedIn()) {
  await router.push({ name: 'home' })
}

const countries = ref<Array<CountryDto>>([])
const errorMessages = ref<{ [key: string]: string }>({})
const didCountryLoadFail = ref<boolean>(false)

try {
  countries.value = await countryApi.getCountries()
} catch (error) {
  didCountryLoadFail.value = true
}

const formState = reactive({
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
  confirmedPassword: undefined
})

const passwordRequirements = computed(() => [
  {
    name: 'Must have at least 8 characters',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.length >= 8
    }
  },
  {
    name: 'Must not contain whitespaces',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw != '' && !pw.includes(' ')
    }
  },
  {
    name: 'Must contain at least one digit',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return /\d/.test(pw)
    }
  },
  {
    name: 'Must contain at least one special character',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return /\W/.test(pw)
    }
  },
  {
    name: 'Must contain at least one uppercase letter',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.toLowerCase() !== pw
    }
  },
  {
    name: 'Must contain at least one lowercase letter',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw.toUpperCase() !== pw
    }
  },
  {
    name: 'Passwords must match',
    predicate: () => {
      if (formState.password == null) {
        return false
      }
      const pw = formState.password as string
      return pw === formState.confirmedPassword
    }
  }
])
const passwordValidator = () => passwordRequirements.value.every((r) => r.predicate())

const rules = {
  firstName: { required: helpers.withMessage('First Name must not be blank', required) },
  lastName: { required: helpers.withMessage('Last Name must not be blank', required) },
  email: {
    required: helpers.withMessage('E-Mail must not be blank', required),
    email: helpers.withMessage('E-Mail is not valid', email)
  },
  phone: { required: helpers.withMessage('Phone must not be blank', required) },
  birthday: { required: helpers.withMessage('Birthday must not be blank', required) },
  street: { required: helpers.withMessage('Street must not be blank', required) },
  houseNumber: {},
  postcode: { required: helpers.withMessage('Postcode must not be blank', required) },
  city: { required: helpers.withMessage('City must not be blank', required) },
  country: { required: helpers.withMessage('Country must not be blank', required) },
  password: {
    required: helpers.withMessage('Password must not be blank', required),
    passwordValidator: helpers.withMessage('Password must fulfill requirements', passwordValidator)
  },
  confirmedPassword: {
    required: helpers.withMessage('Confirmed Password must not be blank', required)
  }
}

const form = useVuelidate(rules, formState)

async function signUp() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const account: AccountDto = {
    firstName: formState.firstName!,
    lastName: formState.lastName!,
    email: formState.email!,
    phone: formState.phone!,
    birthday: formState.birthday!,
    street: formState.street!,
    houseNumber: formState.houseNumber,
    postcode: formState.postcode!,
    city: formState.city!,
    country: formState.country!,
    password: formState.password!
  }

  try {
    await accountApi.signUp(account)
    errorMessages.value = {}
    await router.push({ name: 'home' }) // TODO: change to profile
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
const confirmedPasswordErrors = getErrors('confirmedPassword')
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
