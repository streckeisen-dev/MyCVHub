import { addToast } from '@heroui/react'

export function addSuccessToast(title: string, description?: string) {
  addToast({
    title,
    description,
    color: 'success',
    timeout: 2500
  })
}

export function addErrorToast(title: string, description?: string) {
  addToast({
    title,
    description,
    color: 'danger'
  })
}