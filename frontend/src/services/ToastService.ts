import { TYPE, useToast } from 'vue-toastification'
import type { ToastOptions } from 'vue-toastification/dist/types/types'
import ToastMessage from '@/components/ToastMessage.vue'

export type SuccessOptions = ToastOptions & {
  type?: TYPE.SUCCESS | undefined
}

export type InfoOptions = ToastOptions & {
  type?: TYPE.INFO | undefined
}

export type WarningOptions = ToastOptions & {
  type?: TYPE.WARNING | undefined
}

export type ErrorOptions = ToastOptions & {
  type?: TYPE.ERROR | undefined
}

const displayToast = useToast()

function success(message: string, details?: string, options?: SuccessOptions): void {
  const opt = {
    ...options,
    type: TYPE.SUCCESS
  }
  toast(message, details, opt)
}

function info(message: string, details?: string, options?: InfoOptions): void {
  const opt: InfoOptions = {
    ...options,
    type: TYPE.INFO
  }
  toast(message, details, opt)
}

function warning(message: string, details?: string, options?: WarningOptions): void {
  const opt: WarningOptions = {
    ...options,
    type: TYPE.WARNING
  }
  toast(message, details, opt)
}

function error(message: string, details?: string, options?: ErrorOptions): void {
  const opt: ErrorOptions = {
    ...options,
    timeout: false,
    type: TYPE.ERROR
  }
  toast(message, details, opt)
}

function toast(message: string, details?: string, options?: ToastOptions): void {
  displayToast(
    {
      component: ToastMessage,
      props: {
        message,
        details
      }
    },
    options
  )
}

export default {
  success,
  info,
  warning,
  error
}
