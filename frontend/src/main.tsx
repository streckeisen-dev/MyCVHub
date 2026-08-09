import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import App from './App.tsx'
import { Provider } from './Provider.tsx'
import '@/styles/globals.css'
import '@/config/i18n.ts'
import { ROUTER_BASENAME } from '@/config/RouterConfig.ts'

const root = document.getElementById('root')
if (!root) throw new Error('Root element not found!')
ReactDOM.createRoot(root).render(
  <React.StrictMode>
    <BrowserRouter basename={ROUTER_BASENAME}>
      <Provider>
        <App />
      </Provider>
    </BrowserRouter>
  </React.StrictMode>
)
