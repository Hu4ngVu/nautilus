<script setup lang="ts">
import type { Page, ServerSummary } from '~/types/api'
import type { ServerQueryState, SortKey } from '~/composables/useServerQuery'

const { t, n } = useI18n()
const servers = useServerApi()
const { state, update, params, pageSize } = useServerQuery()
const { messageFor } = useApiError()

useSeoMeta({ title: () => t('browse.title') })

const { data, status, error, refresh } = useAsyncData<Page<ServerSummary>>(
  () => `servers:${state.value.query ?? ''}:${JSON.stringify(params.value)}`,
  () => state.value.query ? servers.search(state.value.query, params.value) : servers.list(params.value)
)

// Search box writes to the URL after a short pause so typing doesn't fire a request per key
const search = ref(state.value.query ?? '')
let debounce: ReturnType<typeof setTimeout> | undefined

watch(search, (value) => {
  clearTimeout(debounce)
  debounce = setTimeout(() => commitSearch(value), 300)
})

watch(() => state.value.query, (q) => {
  if ((q ?? '') !== search.value.trim()) search.value = q ?? ''
})

function commitSearch(value = search.value) {
  clearTimeout(debounce)
  const query = value.trim() || undefined
  if (query !== state.value.query) update({ query })
}

onBeforeUnmount(() => clearTimeout(debounce))

const sortItems = computed(() => [
  { label: t('filters.mostStars'), value: 'starCount,desc' },
  { label: t('filters.fewestStars'), value: 'starCount,asc' }
])

const sort = computed({
  get: () => state.value.sort,
  set: v => update({ sort: v as SortKey })
})

const page = computed({
  get: () => state.value.page,
  set: (p: number) => {
    update({ page: p })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})

const view = useBrowseView()

/** Applied filters as removable chips; search text has its own box and is not repeated here. */
const activeFilters = computed(() => {
  const s = state.value
  const chips: { key: string, label: string, patch: Partial<ServerQueryState> }[] = []
  if (s.serverPlatform) chips.push({ key: 'platform', label: PLATFORM_LABEL[s.serverPlatform], patch: { serverPlatform: undefined } })
  if (s.serverType) chips.push({ key: 'type', label: t(`server.type.${s.serverType}`), patch: { serverType: undefined } })
  if (s.requiresPremium !== undefined) {
    chips.push({
      key: 'premium',
      label: s.requiresPremium ? t('filters.premiumOnly') : t('filters.crackedAllowed'),
      patch: { requiresPremium: undefined }
    })
  }
  for (const tag of s.tags ?? []) {
    chips.push({ key: `tag:${tag}`, label: t(`tags.label.${tag}`, tag), patch: { tags: s.tags?.filter(x => x !== tag) } })
  }
  return chips
})

const filtersOpen = ref(false)
const activeFilterCount = computed(() =>
  [state.value.serverPlatform, state.value.serverType, state.value.requiresPremium].filter(v => v !== undefined).length
  + (state.value.tags?.length ?? 0)
)
</script>

<template>
  <UContainer class="py-6">
    <h1 class="sr-only">
      {{ t('browse.title') }}
    </h1>

    <div class="flex flex-col gap-6 lg:flex-row lg:items-start">
      <!-- Capped to the viewport so the sticky panel scrolls inside instead of running off-screen -->
      <aside class="hidden w-72 shrink-0 lg:sticky lg:top-[calc(var(--ui-header-height)+1.5rem)] lg:flex lg:max-h-[calc(100dvh-var(--ui-header-height)-3rem)] lg:flex-col">
        <ServerFilters
          :state="state"
          @update="update"
        />
      </aside>

      <section class="flex min-w-0 flex-1 flex-col gap-4">
        <div class="flex flex-col gap-2 sm:flex-row">
          <form
            role="search"
            class="flex-1"
            @submit.prevent="commitSearch()"
          >
            <UInput
              v-model="search"
              icon="i-lucide-search"
              :placeholder="t('browse.searchPlaceholder')"
              :aria-label="t('browse.search')"
              variant="soft"
              size="lg"
              class="w-full"
            />
          </form>
          <div class="flex gap-2">
            <USelect
              v-model="sort"
              :items="sortItems"
              icon="i-lucide-arrow-down-wide-narrow"
              :aria-label="t('filters.sort')"
              variant="soft"
              size="lg"
              class="flex-1 sm:w-44"
            />
            <UFieldGroup
              size="lg"
              class="hidden sm:flex"
            >
              <UButton
                icon="i-lucide-list"
                :aria-label="t('browse.viewList')"
                :aria-pressed="view === 'list'"
                color="neutral"
                :variant="view === 'list' ? 'solid' : 'soft'"
                @click="view = 'list'"
              />
              <UButton
                icon="i-lucide-layout-grid"
                :aria-label="t('browse.viewGrid')"
                :aria-pressed="view === 'grid'"
                color="neutral"
                :variant="view === 'grid' ? 'solid' : 'soft'"
                @click="view = 'grid'"
              />
            </UFieldGroup>
            <UButton
              icon="i-lucide-sliders-horizontal"
              :label="activeFilterCount ? `${t('browse.filters')} (${activeFilterCount})` : t('browse.filters')"
              color="neutral"
              variant="soft"
              size="lg"
              class="lg:hidden"
              @click="filtersOpen = true"
            />
          </div>
        </div>

        <div
          v-if="activeFilters.length"
          class="flex flex-wrap items-center gap-2"
          :aria-label="t('browse.activeFilters')"
          role="group"
        >
          <UButton
            v-for="chip in activeFilters"
            :key="chip.key"
            :label="chip.label"
            :aria-label="t('browse.removeFilter', { name: chip.label })"
            trailing-icon="i-lucide-x"
            color="neutral"
            variant="soft"
            size="xs"
            @click="update(chip.patch)"
          />
        </div>

        <div
          v-if="data && data.page.totalElements"
          class="flex flex-wrap items-center justify-between gap-2"
        >
          <p
            class="text-sm text-muted"
            aria-live="polite"
          >
            {{ t('browse.results', { n: n(data.page.totalElements) }, data.page.totalElements) }}
          </p>
          <UPagination
            v-if="data.page.totalPages > 1"
            v-model:page="page"
            :total="data.page.totalElements"
            :items-per-page="pageSize"
            size="sm"
          />
        </div>

        <EmptyState
          v-if="error"
          icon="i-lucide-cloud-off"
          :title="t('errors.loadFailed')"
          :description="messageFor(error)"
        >
          <UButton
            :label="t('common.retry')"
            color="neutral"
            variant="soft"
            class="mt-2"
            @click="refresh()"
          />
        </EmptyState>

        <EmptyState
          v-else-if="status === 'success' && !data?.content.length"
          icon="i-lucide-search-x"
          :title="t('browse.empty')"
          :description="t('browse.emptyHint')"
        />

        <ServerList
          v-else
          :servers="data?.content ?? []"
          :loading="status === 'pending'"
          :view="view"
        />

        <UPagination
          v-if="data && data.page.totalPages > 1"
          v-model:page="page"
          :total="data.page.totalElements"
          :items-per-page="pageSize"
          class="self-center"
        />
      </section>
    </div>

    <USlideover
      v-model:open="filtersOpen"
      :title="t('browse.filters')"
      side="left"
      :ui="{ body: 'flex flex-col overflow-hidden p-0 sm:p-0' }"
    >
      <template #body>
        <ServerFilters
          :state="state"
          :framed="false"
          @update="update($event); filtersOpen = false"
        />
      </template>
    </USlideover>
  </UContainer>
</template>
