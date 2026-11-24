export type PasswordStrength = 0 | 1 | 2 | 3 | 4

/**
 * 0 for empty, otherwise one point per criterion: length >= 8, mixed case, a digit, a symbol.
 * Anything shorter than 8 caps at 1 since the backend rejects it anyway.
 */
export function passwordStrength(password: string): PasswordStrength {
  if (!password) return 0
  const score = [
    password.length >= 8,
    /[a-z]/.test(password) && /[A-Z]/.test(password),
    /\d/.test(password),
    /[^\p{L}\p{N}]/u.test(password)
  ].filter(Boolean).length
  if (password.length < 8) return 1
  return Math.max(1, score) as PasswordStrength
}
