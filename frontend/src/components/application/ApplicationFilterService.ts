const STATUS_FILTER_KEY = 'mycv_application_status_filter'
const SEARCH_TERM_KEY = 'mycv_application_search_term'
const RESULTS_PER_PAGE = 'mycv_application_results_per_page'
const INCLUDE_ARCHIVED_FILTER = 'mycv_application_include_archived'

function getApplicationStatusFilter(): string | undefined {
  return localStorage.getItem(STATUS_FILTER_KEY) ?? undefined
}

function setApplicationStatusFilter(status: string | undefined) {
  if (status) {
    localStorage.setItem(STATUS_FILTER_KEY, status)
  } else {
    localStorage.removeItem(STATUS_FILTER_KEY)
  }
}

function getApplicationSearchTermFilter(): string | undefined {
  return localStorage.getItem(SEARCH_TERM_KEY) ?? undefined
}

function setApplicationSearchTermFilter(searchTerm: string | undefined) {
  if (searchTerm) {
    localStorage.setItem(SEARCH_TERM_KEY, searchTerm)
  } else {
    localStorage.removeItem(SEARCH_TERM_KEY)
  }
}

function getPageSize(): string | undefined {
  return localStorage.getItem(RESULTS_PER_PAGE) ?? undefined
}

function setPageSize(resultsPerPage: string | undefined) {
  if (resultsPerPage) {
    localStorage.setItem(RESULTS_PER_PAGE, resultsPerPage)
  } else {
    localStorage.removeItem(RESULTS_PER_PAGE)
  }
}

function getIncludeArchivedFilter(): boolean | undefined {
  const value = localStorage.getItem(INCLUDE_ARCHIVED_FILTER)
  if (value) {
    return value === 'true'
  }
  return undefined
}

function setIncludeArchivedFilter(includeArchived: boolean | undefined) {
  if (includeArchived) {
    localStorage.setItem(INCLUDE_ARCHIVED_FILTER, includeArchived.toString())
  } else {
    localStorage.removeItem(INCLUDE_ARCHIVED_FILTER)
  }
}

export default {
  getApplicationStatusFilter,
  setApplicationStatusFilter,
  getApplicationSearchTermFilter,
  setApplicationSearchTermFilter,
  getPageSize,
  setPageSize,
  getIncludeArchivedFilter,
  setIncludeArchivedFilter
}
