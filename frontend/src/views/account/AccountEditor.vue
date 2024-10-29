<template>
  <v-row>
    <v-col
      cols="12"
      md="6"
    >
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
    <v-col
      cols="12"
      md="6"
    >
      <v-row>
        <v-col cols="12">
          <h2>{{ t('account.address') }}</h2>
        </v-col>
      </v-row>
      <v-row>
        <v-col
          cols="12"
          sm="9"
        >
          <v-text-field
            v-model="formState.street"
            :label="t('fields.street')"
            :error-messages="streetErrors"
          />
        </v-col>
        <v-col
          cols="12"
          sm="3"
        >
          <v-text-field
            v-model="formState.houseNumber"
            :label="t('fields.houseNumber')"
            :error-messages="houseNumberErrors"
          />
        </v-col>
        <v-col
          cols="12"
          sm="3"
        >
          <v-text-field
            v-model="formState.postcode"
            :label="t('fields.postcode')"
            :error-messages="postcodeErrors"
          />
        </v-col>
        <v-col
          cols="12"
          sm="9"
        >
          <v-text-field
            v-model="formState.city"
            :label="t('fields.city')"
            :error-messages="cityErrors"
          />
        </v-col>
        <v-col
          cols="12"
          sm="12"
        >
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
</template>

<script setup lang="ts">
import { VDateInput } from 'vuetify/labs/components'
import { type ComputedRef, ref, watch } from 'vue'
import type { AccountEditorData } from '@/dto/AccountEditorData'
import type { Validation } from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import { useI18n } from 'vue-i18n'
import type { CountryDto } from '@/dto/CountryDto'
import countryApi from '@/api/CountryApi'
import { useLocale } from 'vuetify'
import toastService from '@/services/ToastService'

const { t } = useI18n({
  useScope: 'global'
})

const form = defineModel<Validation>('form', { required: true })
const formState = defineModel<AccountEditorData>('formState', { required: true })
const errorMessages = defineModel<ErrorMessages>('errorMessages', { required: true })

const countries = ref<Array<CountryDto>>([])
async function loadCountries() {
  try {
    countries.value = await countryApi.getCountries()
  } catch (error) {
    toastService.error(t('country.loadingError.title'), t('country.loadingError.message'))
  }
}
await loadCountries()

watch(useLocale().current, async () => {
  await loadCountries()
})

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
</script>

<style scoped lang="scss"></style>
