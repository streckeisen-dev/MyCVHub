import './assets/main.scss'
import 'vue-toastification/dist/index.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import vuetify from './plugins/vuetify'
import i18n from '@/plugins/i18n'
import Toast, { POSITION } from 'vue-toastification'

const app = createApp(App)

app.use(router)
app.use(vuetify)
app.use(i18n)
app.use(Toast, {
  position: POSITION.TOP_RIGHT
})

app.mount('#app')
