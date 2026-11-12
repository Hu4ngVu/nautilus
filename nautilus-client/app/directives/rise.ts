import type { Directive } from 'vue'

// One observer shared by every v-rise element on the page
let observer: IntersectionObserver | undefined

function reveal(el: HTMLElement) {
  el.classList.remove('rise-pending')
  el.classList.add('animate-rise')
}

function getObserver() {
  observer ??= new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue
      reveal(entry.target as HTMLElement)
      observer!.unobserve(entry.target)
    }
  }, { threshold: 0.1 })
  return observer
}

/** `v-rise="index"`: rise in once when scrolled into view, staggered by index. */
export const vRise: Directive<HTMLElement, number | undefined> = {
  mounted(el, binding) {
    el.style.setProperty('--i', String(binding.value ?? 0))
    if (typeof IntersectionObserver === 'undefined') return
    el.classList.add('rise-pending')
    getObserver().observe(el)
  },
  updated(el, binding) {
    el.style.setProperty('--i', String(binding.value ?? 0))
  },
  beforeUnmount(el) {
    observer?.unobserve(el)
  }
}
