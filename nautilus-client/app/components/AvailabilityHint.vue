<script setup lang="ts">
import type { Availability } from '~/composables/useAvailability'

// Inline result of useAvailability, for a form field's hint slot
defineProps<{ status: Availability }>()

const { t } = useI18n()

const STATUS_STYLE = {
  checking: { icon: 'i-lucide-loader-circle', class: 'text-muted [&>svg]:animate-spin', key: 'auth.checking' },
  available: { icon: 'i-lucide-check', class: 'text-primary', key: 'auth.available' },
  taken: { icon: 'i-lucide-x', class: 'text-error', key: 'auth.taken' },
  error: { icon: 'i-lucide-circle-help', class: 'text-muted', key: 'auth.checkFailed' }
} as const
</script>

<template>
  <span
    class="inline-flex items-center gap-1 text-xs"
    :class="status !== 'idle' ? STATUS_STYLE[status].class : ''"
    aria-live="polite"
  >
    <template v-if="status !== 'idle'">
      <UIcon
        :name="STATUS_STYLE[status].icon"
        class="size-3.5"
      />
      {{ t(STATUS_STYLE[status].key) }}
    </template>
  </span>
</template>
