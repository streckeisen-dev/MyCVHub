export function toShortDate(dateString: string | undefined): string {
  if (dateString == null) {
    return 'Today'
  }
  const date = new Date(dateString)
  return date.toLocaleDateString(navigator.language, {
    month: 'short',
    year: 'numeric'
  })
}

export function convertStringToDate(dateString: string | undefined): Date | undefined {
  if (dateString) {
    return new Date(dateString)
  }
  return undefined
}

export function convertDateToString(date: Date | undefined): string | undefined {
  if (date) {
    const month = date.getMonth() < 9 ? `0${date.getMonth() + 1}` : date.getMonth() + 1
    const day = date.getDate() <= 9 ? `0${date.getDate()}` : date.getDate()
    return `${date.getFullYear()}-${month}-${day}`
  }
  return undefined
}

export function compareDatesByYearAndMonth(
  dateA: Date | undefined,
  dateB: Date | undefined
): number {
  if (dateA === undefined || dateB === undefined) {
    if (dateA !== undefined && dateB === undefined) {
      return 1
    }

    if (dateA === undefined && dateB !== undefined) {
      return -1
    }
    return 0
  }

  if (dateA?.getFullYear() === dateB?.getFullYear() && dateA?.getMonth() === dateB?.getMonth()) {
    return 0
  }

  if (dateA > dateB) {
    return -1
  } else if (dateA < dateB) {
    return 1
  }
  return 0
}
