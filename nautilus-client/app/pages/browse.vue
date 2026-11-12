<script setup lang="ts">
import type { Page, ServerSummary } from '~/types/api'
import type { SortKey } from '~/composables/useServerQuery'

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
      <aside class="hidden w-72 shrink-0 lg:sticky lg:top-[calc(var(--ui-header-height)+1.5rem)] lg:block">
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
          v-if="data && data.totalElements"
          class="flex flex-wrap items-center justify-between gap-2"
        >
          <p
            class="text-sm text-muted"
            aria-live="polite"
          >
            {{ t('browse.results', { n: n(data.totalElements) }, data.totalElements) }}
          </p>
          <UPagination
            v-if="data.totalPages > 1"
            v-model:page="page"
            :total="data.totalElements"
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
        />

        <UPagination
          v-if="data && data.totalPages > 1"
          v-model:page="page"
          :total="data.totalElements"
          :items-per-page="pageSize"
          class="self-center"
        />
      </section>
    </div>

    <USlideover
      v-model:open="filtersOpen"
      :title="t('browse.filters')"
      side="left"
    >
      <template #body>
        <ServerFilters
          :state="state"
          @update="update"
        />
      </template>
    </USlideover>
  </UContainer>
</template>
