import { Toast } from '@heroui/react'

export function addSuccessToast(title: string, description?: string) {
  Toast.toast.success(description ? `${title}: ${description}` : title, { timeout: 2500 })
}

export function addErrorToast(title: string, description?: string) {
  Toast.toast.danger(description ? `${title}: ${description}` : title)
}
