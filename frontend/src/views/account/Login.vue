<template>
  <v-main>
    <v-container id="login-container">
      <v-row justify="center">
        <h1>Login to MyCV</h1>
      </v-row>
      <v-row justify="center">
        <v-sheet id="login-sheet" elevation="12" border rounded>
          <v-form @submit.prevent>
            <v-text-field
              v-model="formState.email"
              label="E-Mail Address"
              :error-messages="emailErrors"
            />
            <password-input
              v-model="formState.password"
              label="Password"
              :error-messages="passwordErrors"
            />
            <v-btn type="submit" block color="primary" @click="login">Login</v-btn>
          </v-form>
          <p>
            Don't have an account yet?
            <router-link :to="{ name: 'signup' }">Sign up now</router-link>
          </p>
        </v-sheet>
      </v-row>
      <notification v-if="errorMessage" title="Login failed" :message="errorMessage" />
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { computed, type ComputedRef, reactive, ref } from 'vue'
import accountApi from '@/api/AccountApi'
import router from '@/router'
import type { ErrorDto } from '@/dto/ErrorDto'
import Notification from '@/components/Notification.vue'
import PasswordInput from '@/components/PasswordInput.vue'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate, { type ErrorObject } from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import { refreshToken } from '@/api/ApiHelper'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const props = defineProps<{
  redirect?: string
}>()

async function forwardAfterSuccessfulLogin() {
  console.log('previousRoute', props.redirect)
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
  email?: string,
  password?: string
}

const formState = reactive<FormState>({
  email: undefined,
  password: undefined
})

const rules = {
  email: {
    required: helpers.withMessage('Password must not be blank', required)
  },
  password: {
    required: helpers.withMessage('Password must not be blank', required)
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
