export const ROUTER_BASENAME = '/ui'

export function withRouterBasename(path: string): string {
  if (!path.startsWith('/') || path === ROUTER_BASENAME || path.startsWith(`${ROUTER_BASENAME}/`)) {
    return path
  }

  return `${ROUTER_BASENAME}${path}`
}
