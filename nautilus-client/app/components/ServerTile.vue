<script setup lang="ts">
import type { ServerSummary } from '~/types/api'

// Grid card with banner; the title link is stretched over the card so `actions` stay outside it
const props = withDefaults(defineProps<{
  server: ServerSummary
  index?: number
  showVisibility?: boolean
}>(), {
  index: 0,
  showVisibility: false
})

const { t, n } = useI18n()

const to = computed(() => serverPath(props.server.username, props.server.servername))
const bedrock = computed(() => props.server.serverPlatform === 'BEDROCK')
const tags = computed(() => props.server.tags ?? [])
const platform = computed(() => [
  props.server.serverPlatform ? PLATFORM_LABEL[props.server.serverPlatform] : null,
  props.server.primaryVersion
].filter(Boolean).join(' '))
</script>

<template>
  <article
    v-rise="index"
    class="group relative flex h-full flex-col overflow-hidden block-surface block-lift"
    :class="bedrock ? 'lift-secondary' : 'lift-primary'"
  >
    <div class="relative aspect-[16/5] border-b-2 border-(--block-border)">
      <MediaImage
        :src="server.bannerUrl"
        :alt="t('server.banner')"
        :name="server.servername"
        banner
      />
      <UBadge
        v-if="showVisibility"
        :label="server.visibility === false ? t('server.private') : t('server.public')"
        :icon="server.visibility === false ? 'i-lucide-lock' : 'i-lucide-globe'"
        :color="server.visibility === false ? 'warning' : 'neutral'"
        variant="solid"
        size="sm"
        class="absolute end-2 top-2"
      />
    </div>

    <div class="flex flex-1 gap-3 p-3">
      <div class="relative -mt-8 size-12 shrink-0 overflow-hidden rounded-md border-2 border-(--block-surface) outline-2 outline-(--block-border)">
        <MediaImage
          :src="server.avatarUrl"
          :alt="server.servername"
          :name="server.servername"
        />
      </div>
      <div class="flex min-w-0 flex-1 flex-col gap-1">
        <h3 class="truncate font-pixel text-pixel-lg text-highlighted">
          <NuxtLink
            :to="to"
            class="after:absolute after:inset-0 focus-visible:outline-none group-hover:underline"
          >
            {{ server.servername }}
          </NuxtLink>
        </h3>
        <p class="flex min-w-0 flex-wrap items-center gap-x-2 text-xs text-muted">
          <span
            class="inline-flex items-center gap-1"
            :aria-label="t('server.stars', { n: n(server.starCount) }, server.starCount)"
          >
            <UIcon
              name="i-lucide-star"
              class="size-3.5"
            />
            {{ n(server.starCount) }}
          </span>
          <span
            v-if="platform"
            class="truncate"
          >{{ platform }}</span>
        </p>
        <p
          v-if="server.summary"
          class="line-clamp-2 text-sm text-toned"
        >
          {{ server.summary }}
        </p>
        <div
          v-if="tags.length"
          class="mt-auto flex flex-wrap gap-1 pt-1"
        >
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
    </div>

    <div
      v-if="$slots.actions"
      class="relative z-10 flex flex-wrap gap-2 px-3 pb-3"
    >
      <slot name="actions" />
    </div>
  </article>
</template>
