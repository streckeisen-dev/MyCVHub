const ADMIN_TIME_ZONE = 'Etc/GMT-1'

export function formatAdminDateTime(value: string, locale: string): string {
  return new Date(value).toLocaleString(locale, { timeZone: ADMIN_TIME_ZONE, timeZoneName: 'short' })
}
