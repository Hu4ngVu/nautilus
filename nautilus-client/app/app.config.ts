// Bright brand green needs dark foreground text to stay readable (WCAG AA)
const darkOnPrimary = [{
  color: 'primary' as const,
  variant: 'solid' as const,
  class: 'text-gray-950'
}]

export default defineAppConfig({
  ui: {
    colors: {
      primary: 'nautilus',
      secondary: 'ocean',
      neutral: 'gray'
    },
    button: {
      compoundVariants: darkOnPrimary
    },
    badge: {
      compoundVariants: darkOnPrimary
    },
    // Design rule: no outlined ("line") cards, no gradients, no glow
    card: {
      defaultVariants: {
        variant: 'soft'
      }
    },
    pageCard: {
      defaultVariants: {
        variant: 'soft'
      }
    }
  }
})
