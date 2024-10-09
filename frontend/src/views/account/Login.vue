<template>
  <v-container id="login-container">
    <v-row justify="center">
      <h1>Login to MyCV</h1>
    </v-row>
    <v-row justify="center">
      <v-sheet id="login-sheet" elevation="12" border rounded color="primary">
        <v-form @submit.prevent>
          <v-text-field
            v-model="formState.email"
            label="E-Mail Address"
            :error-messages="getErrorMessages('email').value"
          />
          <password-input
            v-model="formState.password"
            label="Password"
            :error-messages="getErrorMessages('password').value"
          />
          <v-btn type="submit" block color="btn-primary" @click="login">Login</v-btn>
        </v-form>
        <p>
          Don't have an account yet?
          <router-link :to="{ name: 'signup' }">Sign up now</router-link>
        </p>
      </v-sheet>
    </v-row>
    <notification v-if="errorMessage" title="Login failed" :message="errorMessage" />
  </v-container>
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

if (accountApi.isUserLoggedIn()) {
  await router.push({ name: 'home' })
}

const formState = reactive({
  email: undefined,
  password: undefined
})

const rules = {
  email: {
    required: helpers.withMessage("Password must not be blank", required)
  },
  password: {
    required: helpers.withMessage("Password must not be blank", required)
  }
}

const form = useVuelidate(rules, formState)

const errorMessages = ref<{ [key: string]: string }>({})
const errorMessage = ref<string>()

async function login() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  try {
    await accountApi.login(formState.email!, formState.password!)
    errorMessage.value = ''
    if (router.options.history.state['back']) {
      router.back()
    } else {
      await router.push({ name: 'home' })
    }
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error.errors
    if (Object.keys(errorMessages.value).length === 0) {
      errorMessage.value = error.message
    }
  }
}

function getErrorMessages(attributeName: string): ComputedRef<[string]> {
  return computed(() => {
    const backendError = errorMessages.value[attributeName]
    if (backendError) {
      return [backendError]
    }
    return form.value[attributeName].$errors.map((e: ErrorObject) => e.$message)
  })
}
</script>

<style lang="scss" scoped>
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
      color: rgb(var(--v-theme-btn-primary));
    }
  }
}
</style>
