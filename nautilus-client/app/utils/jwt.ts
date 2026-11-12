export interface AccessTokenClaims {
  sub: string
  role?: string
  exp?: number
}

export function decodeJwt(token: string): AccessTokenClaims | null {
  const payload = token.split('.')[1]
  if (!payload) return null
  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    )
    return JSON.parse(json) as AccessTokenClaims
  } catch {
    return null
  }
}
