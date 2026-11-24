import type { TagCategory } from '~/types/api'
import { BLOCK_PALETTE, type BlockColor } from './blockColor'

// Mirrors nautilus-server `server/constants/Tag.java`; the backend rejects anything else
export const TAG_CATALOG: Record<TagCategory, string[]> = {
  FEATURES: [
    'Bosses', 'Classes', 'Custom Content', 'Dungeons', 'Economy', 'Media', 'OP',
    'Personal Worlds', 'Plots', 'Pokemon', 'PvE', 'PvP', 'Questing', 'Teams'
  ],
  GAMEPLAY: [
    'Anarchy', 'Battle Royale', 'Bed Wars', 'Factions', 'Gens', 'Kit PvP', 'Lifesteal',
    'Microgames', 'Minigames', 'One Block', 'Parkour', 'Prison', 'Racing', 'RPG',
    'Skyblock', 'Towns', 'Vanilla Like'
  ],
  META: [
    'Adventure Mode', 'Creative Mode', 'Crossplay', 'Hardcore Mode', 'Keep Inventory',
    'Network', 'Offline Mode', 'Survival Mode', 'Whitelisted', 'World Resets'
  ],
  COMMUNITY: [
    'Competitive', 'Creator Community', 'MMO', 'Recording SMP', 'Roleplay', 'SMP',
    'Social', 'Technical'
  ]
}

export const TAG_CATEGORIES = Object.keys(TAG_CATALOG) as TagCategory[]

export const MAX_TAGS_PER_CATEGORY = 20

// Category colours from the block palette, kept clear of the Java green and Bedrock blue
export const TAG_CATEGORY_COLOR: Record<TagCategory, BlockColor> = {
  FEATURES: BLOCK_PALETTE[2]!,
  GAMEPLAY: BLOCK_PALETTE[5]!,
  META: BLOCK_PALETTE[4]!,
  COMMUNITY: BLOCK_PALETTE[3]!
}
