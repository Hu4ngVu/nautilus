<script setup lang="ts">
import type { ServerSummary } from '~/types/api'

// Horizontal listing row, modelled on Modrinth's discover results
const props = withDefaults(defineProps<{
  server: ServerSummary
  index?: number
}>(), {
  index: 0
})

const { t, n } = useI18n()

const to = computed(() => serverPath(props.server.username, props.server.servername))
const bedrock = computed(() => props.server.serverPlatform === 'BEDROCK')
const tags = computed(() => props.server.tags ?? [])
</script>

<template>
  <article
    v-rise="index"
    class="group relative flex min-w-0 gap-4 p-3 block-surface block-lift sm:p-4"
    :class="bedrock ? 'lift-secondary' : 'lift-primary'"
  >
    <div class="size-16 shrink-0 overflow-hidden rounded-md border-2 border-(--block-border) sm:size-24">
      <MediaImage
        :src="server.avatarUrl"
        :alt="server.servername"
        :name="server.servername"
      />
    </div>

    <div class="flex min-w-0 flex-1 flex-col gap-1.5">
      <p class="flex min-w-0 flex-wrap items-baseline gap-x-2">
        <NuxtLink
          :to="to"
          class="min-w-0 truncate font-pixel text-pixel-xl text-highlighted after:absolute after:inset-0 focus-visible:outline-none group-hover:underline"
        >
          {{ server.servername }}
        </NuxtLink>
        <span class="truncate text-sm text-muted">{{ t('server.by', { username: server.username }) }}</span>
      </p>
      <p
        v-if="server.summary"
        class="line-clamp-2 text-sm text-toned"
      >
        {{ server.summary }}
      </p>
      <div class="mt-auto flex flex-wrap gap-1.5">
        <UBadge
          v-if="server.serverPlatform"
          :label="PLATFORM_LABEL[server.serverPlatform]"
          color="secondary"
          variant="solid"
          size="sm"
        />
        <UBadge
          v-if="server.requiresPremium"
          :label="t('server.premium')"
          icon="i-lucide-badge-check"
          color="primary"
          variant="solid"
          size="sm"
        />
        <UBadge
          v-for="tag in tags"
          :key="tag"
          :label="tag"
          color="neutral"
          variant="soft"
          size="sm"
        />
      </div>
    </div>

    <div class="flex shrink-0 flex-col items-end gap-1 text-end">
      <p
        class="flex items-center gap-1 font-pixel text-pixel-xl text-highlighted"
        :aria-label="t('server.stars', { n: n(server.starCount) }, server.starCount)"
      >
        <UIcon
          name="i-lucide-star"
          class="size-4"
        />
        {{ n(server.starCount) }}
      </p>
      <p
        v-if="server.primaryVersion"
        class="text-xs font-semibold text-primary"
      >
        {{ server.primaryVersion }}
      </p>
    </div>
  </article>
</template>
