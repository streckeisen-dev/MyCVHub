import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import App from './App.tsx'
import { Provider } from './Provider.tsx'
import '@/styles/globals.css'
import '@/config/i18n.ts'

const root = document.getElementById('root')
if (!root) throw new Error('Root element not found!')
ReactDOM.createRoot(root).render(
  <React.StrictMode>
    <BrowserRouter basename="/ui">
      <Provider>
        <App />
      </Provider>
    </BrowserRouter>
  </React.StrictMode>
)
