export interface BlockColor {
  face: string
  shade: string
  light: string
  ink: string
}

// Brand-friendly block palette; ink is the text colour that stays readable on the face
export const BLOCK_PALETTE: BlockColor[] = [
  { face: '#73ff9c', shade: '#00bc39', light: '#b3ffc9', ink: '#03130a' },
  { face: '#9fb1fc', shade: '#2f58f9', light: '#c5d1fe', ink: '#0b1647' },
  { face: '#fcd34d', shade: '#d97706', light: '#fef3c7', ink: '#451a03' },
  { face: '#fda4af', shade: '#e11d48', light: '#ffe4e6', ink: '#4c0519' },
  { face: '#c4b5fd', shade: '#7c3aed', light: '#ede9fe', ink: '#2e1065' },
  { face: '#5eead4', shade: '#0d9488', light: '#ccfbf1', ink: '#042f2e' }
]

/** Stable colour for a name (FNV-1a), so a server keeps the same placeholder everywhere. */
export function blockColor(name: string): BlockColor {
  let hash = 0x811C9DC5
  for (let i = 0; i < name.length; i++) {
    hash ^= name.charCodeAt(i)
    hash = Math.imul(hash, 0x01000193)
  }
  return BLOCK_PALETTE[(hash >>> 0) % BLOCK_PALETTE.length]!
}
