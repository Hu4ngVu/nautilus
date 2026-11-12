// Mirrors backend bean validation constraints
export const USERNAME_PATTERN = /^[\w@.]{3,20}$/
export const SERVERNAME_PATTERN = /^[\w@.]{1,50}$/
export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const ADDRESS_PATTERN = /^[a-z0-9][a-z0-9.]*[a-z0-9](:\d{1,5})?$/i

export const LIMITS = {
  password: 50,
  servername: 50,
  summary: 255,
  bio: 10000,
  address: 50,
  imageTitle: 64,
  imageSummary: 255
} as const
