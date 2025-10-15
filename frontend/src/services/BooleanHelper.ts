export function toBoolean(s: string | undefined | null): boolean | undefined {
  if (s === 'true') {
    return true
  }
  if (s === 'false') {
    return false
  }
  return undefined
}