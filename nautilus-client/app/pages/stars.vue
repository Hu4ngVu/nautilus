<script setup lang="ts">
import type { StarredServer } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { t } = useI18n()
const auth = useAuthStore()
const servers = useServerApi()
const stars = useStarsStore()
const toast = useToast()
const { messageFor, notify } = useApiError()

useSeoMeta({ title: () => t('stars.title') })

const PAGE_SIZE = 20
const page = ref(1)

const { data, status, error, refresh } = useAsyncData(
  () => `stars:${auth.username}:${page.value}`,
  () => servers.starred(auth.username!, { page: page.value - 1, size: PAGE_SIZE })
)

// Local copy so unstarring removes a card immediately and undo can put it back in place
const items = ref<StarredServer[]>([])
watch(data, (value) => {
  items.value = [...(value?.content ?? [])]
}, { immediate: true })

const keyOf = (s: StarredServer) => `${s.username}/${s.servername}`
const restoring = new Set<string>()

function insertAt(index: number, server: StarredServer) {
  const next = [...items.value]
  next.splice(Math.min(index, next.length), 0, server)
  items.value = next
}

async function unstar(server: StarredServer) {
  const index = items.value.findIndex(s => keyOf(s) === keyOf(server))
  if (index < 0) return
  items.value = items.value.filter((_, i) => i !== index)
  try {
    await stars.unstar(server.username, server.servername)
  } catch (err) {
    insertAt(index, server)
    notify(err)
    return
  }
  toast.add({
    title: t('stars.unstarred'),
    description: server.servername,
    icon: 'i-lucide-star-off',
    actions: [{ label: t('stars.undo'), color: 'neutral', variant: 'soft', onClick: () => undo(index, server) }]
  })
  // An emptied page may still have stars after it (or before it): fetch the right page again
  if (!items.value.length) {
    if (page.value > 1) page.value--
    else await refresh()
  }
}

async function undo(index: number, server: StarredServer) {
  const key = keyOf(server)
  // Guard double clicks and a card that is already back
  if (restoring.has(key) || items.value.some(s => keyOf(s) === key)) return
  restoring.add(key)
  try {
    await stars.star(server.username, server.servername)
    insertAt(index, server)
    toast.add({ title: t('stars.restored'), description: server.servername, icon: 'i-lucide-star', color: 'success' })
  } catch {
    toast.add({ title: t('stars.undoFailed'), icon: 'i-lucide-circle-alert', color: 'error' })
  } finally {
    restoring.delete(key)
  }
}
</script>

<template>
  <UContainer class="py-8">
    <PageHeader
      :title="t('stars.title')"
      :description="t('stars.subtitle')"
    />

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
      v-else-if="status === 'success' && !items.length"
      icon="i-lucide-star"
      :title="t('stars.empty')"
      :description="t('stars.emptyHint')"
    >
      <UButton
        to="/browse"
        icon="i-lucide-compass"
        :label="t('home.browse')"
        class="mt-2"
      />
    </EmptyState>

    <div
      v-else
      class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3"
    >
      <template v-if="status === 'pending' && !data">
        <USkeleton
          v-for="i in 6"
          :key="i"
          class="h-28"
        />
      </template>
      <ServerRepoCard
        v-for="(s, i) in items"
        v-else
        :key="keyOf(s)"
        :username="s.username"
        :servername="s.servername"
        :avatar-url="s.serverAvatarUrl"
        :star-count="s.serverTotalStar"
        :summary="t('server.by', { username: s.username })"
        :index="i"
      >
        <template #actions>
          <UButton
            icon="i-lucide-star-off"
            :aria-label="`${t('server.unstar')} ${s.servername}`"
            color="warning"
            variant="soft"
            size="sm"
            @click="unstar(s)"
          />
        </template>
      </ServerRepoCard>
    </div>

    <UPagination
      v-if="data && data.page.totalPages > 1"
      v-model:page="page"
      :total="data.page.totalElements"
      :items-per-page="PAGE_SIZE"
      class="mt-6 flex justify-center"
    />
  </UContainer>
</template>
