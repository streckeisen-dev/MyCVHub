const AUTH_STATE_KEY = 'my-cv-login-state'

function successfulLogin() {
	localStorage.setItem(AUTH_STATE_KEY, 'true')
}

function loggedOut() {
	localStorage.removeItem(AUTH_STATE_KEY)
}

function isLoggedIn(): boolean {
	return Boolean(localStorage.getItem(AUTH_STATE_KEY)) || false
}

export default {
	successfulLogin,
	loggedOut,
	isLoggedIn
}
