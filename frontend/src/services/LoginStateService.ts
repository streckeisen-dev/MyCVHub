import { ref } from 'vue'

const loginState = ref(false)

function successfulLogin() {
  loginState.value = true
}

function loggedOut() {
  loginState.value = false
}

function isLoggedIn(): boolean {
  return loginState.value
}

export default {
  successfulLogin,
  loggedOut,
  isLoggedIn
}
