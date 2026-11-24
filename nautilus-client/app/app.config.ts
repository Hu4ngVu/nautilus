// Blocky Modern theme: 2px block borders, solid offset shadows, pixel type on controls.
// Classes here are merged into Nuxt UI's theme with tailwind-merge, so prefer plain utilities
// (bg-*, border-*, shadow-*) that it can de-duplicate against the defaults.

const pixelText = ['pixel-sm', 'pixel-base', 'pixel-lg', 'pixel-xl', 'pixel-2xl', 'pixel-3xl', 'pixel-4xl']

const pressMotion = 'block-press transition-[transform,box-shadow,background-color,color] duration-150 ease-[cubic-bezier(.3,1.6,.6,1)]'

// Bright brand green needs dark foreground text to stay readable (WCAG AA)
const solidPrimary = 'bg-nautilus-400 hover:bg-nautilus-300 active:bg-nautilus-400 disabled:bg-nautilus-400 aria-disabled:bg-nautilus-400 text-gray-950'

const blockField = 'bg-(--block-surface) ring-0 border-2 border-(--block-border) rounded-md transition-shadow duration-150 focus-visible:ring-0 focus-visible:shadow-[4px_4px_0_var(--color-nautilus-500)]'

const blockOverlay = 'bg-(--block-surface) ring-0 border-2 border-(--block-border) rounded-lg shadow-[6px_6px_0_var(--lift-ink)]'

export default defineAppConfig({
  ui: {
    colors: {
      primary: 'nautilus',
      secondary: 'ocean',
      neutral: 'gray'
    },
    tv: {
      twMergeConfig: {
        extend: {
          theme: {
            text: pixelText,
            font: ['pixel']
          }
        }
      }
    },
    button: {
      slots: {
        base: 'font-pixel leading-none rounded-md border-2 border-transparent'
      },
      variants: {
        size: {
          xs: { base: 'text-pixel-sm' },
          sm: { base: 'text-pixel-sm' },
          md: { base: 'text-pixel-base' },
          lg: { base: 'text-pixel-lg' },
          xl: { base: 'text-pixel-xl' }
        }
      },
      compoundVariants: [
        { variant: 'solid', class: `${pressMotion} lift-ink border-(--lift-ink)` },
        { variant: ['outline', 'soft', 'subtle'], class: `${pressMotion} lift-ink border-(--block-border) ring-0` },
        { color: 'neutral', variant: ['outline', 'soft', 'subtle'], class: 'bg-(--block-surface) hover:bg-elevated active:bg-(--block-surface) text-default' },
        { color: 'primary', variant: 'solid', class: solidPrimary },
        { color: 'warning', variant: 'solid', class: 'bg-amber-300 hover:bg-amber-200 active:bg-amber-300 text-amber-950' }
      ]
    },
    badge: {
      slots: {
        base: 'rounded-sm'
      },
      compoundVariants: [
        { color: 'primary', variant: 'solid', class: 'bg-nautilus-400 text-gray-950' },
        { color: 'warning', variant: 'solid', class: 'bg-amber-300 text-gray-950' }
      ]
    },
    card: {
      slots: {
        root: 'border-2 border-(--block-border) rounded-lg ring-0',
        header: 'border-(--block-border)',
        footer: 'border-(--block-border)'
      },
      variants: {
        variant: {
          soft: { root: 'bg-(--block-surface) divide-(--block-border)' },
          outline: { root: 'bg-(--block-surface) ring-0 divide-(--block-border)' },
          subtle: { root: 'bg-(--block-surface) ring-0 divide-(--block-border)' }
        }
      },
      defaultVariants: {
        variant: 'soft'
      }
    },
    pageCard: {
      defaultVariants: {
        variant: 'soft'
      }
    },
    input: {
      slots: { base: blockField },
      compoundVariants: [
        { variant: ['outline', 'soft', 'subtle'], class: { base: blockField } }
      ]
    },
    textarea: {
      slots: { base: blockField },
      compoundVariants: [
        { variant: ['outline', 'soft', 'subtle'], class: { base: blockField } }
      ]
    },
    select: {
      slots: {
        base: blockField,
        content: blockOverlay
      },
      compoundVariants: [
        { variant: ['outline', 'soft', 'subtle'], class: { base: blockField } }
      ]
    },
    selectMenu: {
      slots: {
        base: blockField,
        content: blockOverlay
      },
      compoundVariants: [
        { variant: ['outline', 'soft', 'subtle'], class: { base: blockField } }
      ]
    },
    inputMenu: {
      slots: {
        base: blockField,
        content: blockOverlay
      },
      compoundVariants: [
        { variant: ['outline', 'soft', 'subtle'], class: { base: blockField } }
      ]
    },
    tabs: {
      slots: {
        trigger: 'font-pixel',
        label: 'leading-none'
      },
      variants: {
        size: {
          xs: { trigger: 'text-pixel-sm' },
          sm: { trigger: 'text-pixel-sm' },
          md: { trigger: 'text-pixel-base' },
          lg: { trigger: 'text-pixel-base' },
          xl: { trigger: 'text-pixel-lg' }
        }
      },
      compoundVariants: [
        {
          orientation: 'horizontal',
          variant: 'link',
          class: {
            list: 'border-b-2 border-(--block-border) -mb-[2px]',
            indicator: '-bottom-[2px] h-[3px] rounded-none'
          }
        },
        {
          orientation: 'vertical',
          variant: 'link',
          class: {
            list: 'border-s-2 border-(--block-border) -ms-[2px]',
            indicator: '-start-[2px] w-[3px] rounded-none'
          }
        },
        {
          variant: 'pill',
          class: {
            list: 'bg-(--block-surface) border-2 border-(--block-border) rounded-lg',
            indicator: 'rounded-md shadow-none'
          }
        }
      ]
    },
    navigationMenu: {
      slots: {
        link: 'font-pixel text-pixel-base'
      },
      compoundVariants: [
        {
          orientation: 'horizontal',
          highlight: true,
          class: { link: 'after:h-[3px] after:rounded-none' }
        }
      ]
    },
    dropdownMenu: {
      slots: {
        content: blockOverlay,
        item: 'rounded-md'
      }
    },
    popover: {
      slots: { content: blockOverlay }
    },
    tooltip: {
      slots: { content: 'bg-(--block-surface) ring-0 border-2 border-(--block-border) rounded-md shadow-[3px_3px_0_var(--lift-ink)]' }
    },
    modal: {
      slots: {
        content: `${blockOverlay} divide-(--block-border)`,
        title: 'font-pixel text-pixel-xl leading-none'
      }
    },
    slideover: {
      slots: {
        content: 'bg-(--block-surface) ring-0 divide-(--block-border)',
        title: 'font-pixel text-pixel-xl leading-none'
      }
    },
    toast: {
      slots: {
        root: `${blockOverlay} shadow-[4px_4px_0_var(--lift-ink)]`,
        title: 'font-pixel text-pixel-base leading-none'
      }
    },
    authForm: {
      slots: {
        title: 'font-pixel text-pixel-2xl font-normal'
      }
    },
    alert: {
      slots: {
        root: 'border-2 border-(--block-border) rounded-lg',
        title: 'font-medium'
      }
    },
    checkbox: {
      slots: {
        base: 'rounded-sm ring-2 ring-(--block-border) data-[state=checked]:shadow-[inset_-2px_-2px_0_var(--color-nautilus-700)]'
      }
    },
    switch: {
      slots: {
        base: 'border-2 border-(--block-border) rounded-md',
        thumb: 'rounded-sm'
      }
    },
    pagination: {
      slots: {
        list: 'gap-1.5'
      }
    },
    skeleton: {
      base: 'rounded-lg'
    },
    avatar: {
      slots: {
        root: 'rounded-md'
      }
    },
    header: {
      slots: {
        root: 'bg-(--ui-bg)/85 border-b-2 border-(--block-border)'
      }
    },
    footer: {
      slots: {
        root: 'border-t-2 border-(--block-border)'
      }
    }
  }
})
