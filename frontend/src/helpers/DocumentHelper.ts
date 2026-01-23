export function openPdfInNewTab(data: Blob | MediaSource) {
  const fileURL = globalThis.URL.createObjectURL(data)
  const a = document.createElement('a')
  a.href = fileURL
  a.download = 'cv.pdf'
  document.body.appendChild(a)
  a.click()
  a.remove()
  globalThis.URL.revokeObjectURL(fileURL)
}