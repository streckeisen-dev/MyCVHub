/*
 * used to simulate the automatic determination of text color usually done be HeroUI
 * since the text color on non-profile page is determined based on the browser theme, that doesn't work for the preview
 */
export function getMatchingTextColor(backgroundColor: string) {
  const color = backgroundColor.replace('#', '')
  const r = parseInt(color.slice(0, 2), 16)
  const g = parseInt(color.slice(2, 4), 16)
  const b = parseInt(color.slice(4, 6), 16)
  const brightness = (r * 299 + g * 587 + b * 114) / 1000
  return brightness > 155 ? '#000000' : '#FFFFFF'
}