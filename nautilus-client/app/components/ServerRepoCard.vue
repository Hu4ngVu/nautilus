<script setup lang="ts">
import type { ServerPlatform } from '~/types/api'

// Compact GitHub-repository-style card for profile and starred lists
const props = withDefaults(defineProps<{
  username: string
  servername: string
  avatarUrl?: string | null
  starCount: number
  summary?: string | null
  platform?: ServerPlatform | null
  index?: number
}>(), {
  avatarUrl: null,
  summary: null,
  platform: null,
  index: 0
})

const { t, n } = useI18n()
const to = computed(() => serverPath(props.username, props.servername))
</script>

<template>
  <article
    v-rise="index"
    class="group relative flex min-w-0 flex-col gap-2 p-4 block-surface block-lift lift-secondary"
  >
    <div class="flex min-w-0 items-center gap-2">
      <div class="size-7 shrink-0 overflow-hidden rounded-sm border-2 border-(--block-border)">
        <MediaImage
          :src="avatarUrl"
          :alt="servername"
          :name="servername"
        />
      </div>
      <NuxtLink
        :to="to"
        class="min-w-0 flex-1 truncate font-pixel text-pixel-lg text-secondary after:absolute after:inset-0 focus-visible:outline-none group-hover:underline"
      >
        {{ servername }}
      </NuxtLink>
      <div
        v-if="$slots.actions"
        class="relative z-10 shrink-0"
      >
        <slot name="actions" />
      </div>
    </div>
    <p
      v-if="summary"
      class="line-clamp-2 flex-1 text-sm text-toned"
    >
      {{ summary }}
    </p>
    <p class="mt-auto flex items-center gap-3 text-xs text-muted">
      <span
        v-if="platform"
        class="inline-flex items-center gap-1.5"
      >
        <span
          class="size-2"
          :class="platform === 'BEDROCK' ? 'bg-ocean-400' : 'bg-nautilus-500'"
          aria-hidden="true"
        />
        {{ PLATFORM_LABEL[platform] }}
      </span>
      <span
        class="inline-flex items-center gap-1"
        :aria-label="t('server.stars', { n: n(starCount) }, starCount)"
      >
        <UIcon
          name="i-lucide-star"
          class="size-3.5"
        />
        {{ n(starCount) }}
      </span>
    </p>
  </article>
</template>
