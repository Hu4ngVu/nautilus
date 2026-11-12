<script setup lang="ts">
// Horizontal listing row, modelled on Modrinth's discover results
const props = defineProps<{
  username: string
  servername: string
  avatarUrl?: string | null
  summary?: string | null
  starCount: number
  requiresPremium?: boolean
}>()

const { t, n } = useI18n()
const to = computed(() => `/s/${encodeURIComponent(props.username)}/${encodeURIComponent(props.servername)}`)
</script>

<template>
  <NuxtLink
    :to="to"
    class="group flex gap-4 rounded-lg bg-elevated/60 p-4 transition-colors hover:bg-elevated focus-visible:outline-3 outline-primary/40"
  >
    <div class="size-16 shrink-0 overflow-hidden rounded-lg sm:size-24">
      <MediaImage
        :src="avatarUrl"
        :alt="servername"
        :name="servername"
      />
    </div>
    <div class="flex min-w-0 flex-1 flex-col gap-1.5">
      <p class="flex min-w-0 flex-wrap items-baseline gap-x-2">
        <span class="truncate text-lg font-bold text-highlighted group-hover:underline">{{ servername }}</span>
        <span class="truncate text-sm text-muted">{{ t('server.by', { username }) }}</span>
      </p>
      <p
        v-if="summary"
        class="line-clamp-2 text-sm text-toned"
      >
        {{ summary }}
      </p>
      <div
        v-if="requiresPremium"
        class="mt-auto flex flex-wrap gap-1.5"
      >
        <UBadge
          :label="t('server.premium')"
          icon="i-lucide-badge-check"
          color="secondary"
          variant="soft"
          size="sm"
        />
      </div>
    </div>
    <div
      class="flex shrink-0 items-start gap-1 text-sm font-medium text-muted sm:text-base"
      :aria-label="t('server.stars', { n: n(starCount) }, starCount)"
    >
      <UIcon
        name="i-lucide-star"
        class="size-4 sm:mt-0.5"
      />
      {{ n(starCount) }}
    </div>
  </NuxtLink>
</template>
