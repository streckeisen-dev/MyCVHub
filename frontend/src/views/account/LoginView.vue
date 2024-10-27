<template>
  <v-main>
    <v-container id="login-container">
      <v-row justify="center">
        <h1>{{ t('account.login.title') }}</h1>
      </v-row>
      <v-row justify="center">
        <v-sheet
          id="login-sheet"
          elevation="12"
          border
          rounded
        >
          <v-form @submit.prevent>
            <v-text-field
              v-model="formState.email"
              :label="t('fields.email')"
              :error-messages="emailErrors"
            />
            <password-input
              v-model="formState.password"
              :label="t('fields.password')"
              :error-messages="passwordErrors"
            />
            <v-btn
              type="submit"
              block
              color="primary"
              @click="login"
              >{{ t('account.login.action') }}
            </v-btn>
          </v-form>
          <p>
            {{ t('account.login.noAccount') }}
            <router-link :to="{ name: 'signup' }">{{ t('account.login.signup') }}</router-link>
          </p>
        </v-sheet>
      </v-row>
      <notification-message
        v-if="errorMessage"
        :title="t('account.login.error')"
        :message="errorMessage"
      />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import accountApi from '@/api/AccountApi'
import router from '@/router'
import type { ErrorDto } from '@/dto/ErrorDto'
import NotificationMessage from '@/components/NotificationMessage.vue'
import PasswordInput from '@/components/PasswordInput.vue'
import useVuelidate from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import { useI18n } from 'vue-i18n'
import { required } from '@/validation/validators'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  redirect?: string
}>()

async function forwardAfterSuccessfulLogin() {
  if (props.redirect) {
    await router.push({ path: props.redirect })
  } else {
    await router.push({ name: 'account' })
  }
}

if (accountApi.isUserLoggedIn()) {
  await forwardAfterSuccessfulLogin()
}

type FormState = {
  email?: string
  password?: string
}

const formState = reactive<FormState>({
  email: undefined,
  password: undefined
})

const rules = {
  email: {
    required: required
  },
  password: {
    required: required
  }
}

const form = useVuelidate<FormState>(rules, formState)

const errorMessages = ref<ErrorMessages>({})
const errorMessage = ref<string>()

function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const emailErrors = getErrors('email')
const passwordErrors = getErrors('password')

async function login() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  try {
    await accountApi.login(formState.email!, formState.password!)
    errorMessage.value = ''
    await forwardAfterSuccessfulLogin()
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      errorMessage.value = error.message
    }
  }
}
</script>

<style lang="scss" scoped>
.refresh-overlay {
  background-color: rgb(var(--v-theme-surface));
  justify-content: center;

  .refresh-overlay-content {
    position: absolute;
    margin-top: 45vh;
  }
}

#login-container {
  margin-top: 50px;
}

#login-sheet {
  margin-top: 10px;
  width: 100%;
  max-width: 600px;
  padding: 50px;

  p {
    margin-top: 10px;

    a {
      color: rgb(var(--v-theme-primary));
    }
  }
}
</style>
