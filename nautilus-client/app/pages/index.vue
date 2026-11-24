<script setup lang="ts">
import type { LocationQueryRaw } from 'vue-router'

const { t } = useI18n()

useSeoMeta({ title: () => t('home.tagline') })

const search = ref('')
const searchInput = useTemplateRef('searchInput')

function submitSearch() {
  const query = search.value.trim()
  navigateTo({ path: '/browse', query: query ? { query } : undefined })
}

// "/" jumps to search unless the user is already typing somewhere
function onKeydown(event: KeyboardEvent) {
  if (event.key !== '/' || event.ctrlKey || event.metaKey || event.altKey) return
  const target = event.target as HTMLElement | null
  if (target?.closest('input, textarea, select, [contenteditable="true"]')) return
  event.preventDefault()
  searchInput.value?.inputRef?.focus()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))

// Tag names must match utils/tags.ts; labels are translated
const QUICK_TAGS = ['Survival Mode', 'PvP', 'SMP', 'Skyblock', 'Minigames', 'Factions']

const browse = (query: LocationQueryRaw) => ({ path: '/browse', query })

const shelves = computed(() => [
  { key: 'popular', title: t('home.shelves.popular'), filters: {}, to: browse({}) },
  { key: 'survival', title: t('home.shelves.survival'), filters: { tags: ['Survival Mode'] }, to: browse({ tags: 'Survival Mode' }) },
  { key: 'modpack', title: t('home.shelves.modpack'), filters: { serverType: 'MODPACK' as const }, to: browse({ type: 'MODPACK' }) },
  { key: 'bedrock', title: t('home.shelves.bedrock'), filters: { serverPlatform: 'BEDROCK' as const }, to: browse({ platform: 'BEDROCK' }) }
])
</script>

<template>
  <div>
    <UContainer class="flex flex-col items-center gap-6 pt-14 pb-10 text-center sm:pt-20">
      <PixelShell :size="72" />

      <h1 class="max-w-3xl font-pixel text-pixel-3xl text-balance text-highlighted sm:text-pixel-4xl">
        <i18n-t keypath="home.taglineRich">
          <template #highlight>
            <span class="text-primary">{{ t('home.taglineHighlight') }}</span>
          </template>
        </i18n-t>
      </h1>

      <form
        role="search"
        class="w-full max-w-xl"
        @submit.prevent="submitSearch"
      >
        <UInput
          ref="searchInput"
          v-model="search"
          icon="i-lucide-search"
          size="xl"
          :placeholder="t('home.searchPlaceholder')"
          :aria-label="t('browse.search')"
          :aria-keyshortcuts="'/'"
          class="w-full"
          :ui="{ base: 'shadow-[5px_5px_0_var(--color-nautilus-500)] focus-visible:shadow-[5px_5px_0_var(--color-nautilus-500)]' }"
        >
          <template #trailing>
            <UKbd
              value="/"
              :title="t('home.searchShortcut')"
            />
          </template>
        </UInput>
      </form>

      <nav
        :aria-label="t('home.quickTags')"
        class="flex max-w-xl flex-wrap justify-center gap-2"
      >
        <UButton
          v-for="tag in QUICK_TAGS"
          :key="tag"
          :to="browse({ tags: tag })"
          :label="t(`tags.label.${tag}`, tag)"
          color="neutral"
          variant="soft"
          size="sm"
          class="lift-secondary"
        />
      </nav>
    </UContainer>

    <UContainer class="flex flex-col gap-10 pb-16">
      <ServerShelf
        v-for="shelf in shelves"
        :key="shelf.key"
        :title="shelf.title"
        :filters="shelf.filters"
        :to="shelf.to"
      />
    </UContainer>
  </div>
</template>
