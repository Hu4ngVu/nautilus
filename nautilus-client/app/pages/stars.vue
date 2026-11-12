<script setup lang="ts">
import type { StarredServer } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { t, n } = useI18n()
const auth = useAuthStore()
const servers = useServerApi()
const stars = useStarsStore()
const { messageFor, notify, success } = useApiError()

useSeoMeta({ title: () => t('stars.title') })

const PAGE_SIZE = 20
const page = ref(1)

const { data, status, error, refresh } = useAsyncData(
  () => `stars:${auth.username}:${page.value}`,
  () => servers.starred(auth.username!, { page: page.value - 1, size: PAGE_SIZE })
)

const serverPath = (s: StarredServer) => `/s/${encodeURIComponent(s.username)}/${encodeURIComponent(s.servername)}`
const pending = ref(new Set<string>())

async function unstar(s: StarredServer) {
  const key = `${s.username}/${s.servername}`
  pending.value = new Set(pending.value).add(key)
  try {
    await stars.unstar(s.username, s.servername)
    success(t('stars.unstarred'))
    if (data.value?.content.length === 1 && page.value > 1) page.value--
    else await refresh()
  } catch (err) {
    notify(err)
  } finally {
    const next = new Set(pending.value)
    next.delete(key)
    pending.value = next
  }
}
</script>

<template>
  <UContainer class="max-w-4xl py-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-highlighted sm:text-3xl">
        {{ t('stars.title') }}
      </h1>
      <p class="mt-1 text-muted">
        {{ t('stars.subtitle') }}
      </p>
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

    <ul
      v-else
      class="flex flex-col gap-3"
    >
      <template v-if="status === 'pending' && !data">
        <li
          v-for="i in 5"
          :key="i"
        >
          <USkeleton class="h-20 rounded-lg" />
        </li>
      </template>
      <li
        v-for="s in data?.content ?? []"
        v-else
        :key="`${s.username}/${s.servername}`"
        class="flex items-center gap-4 rounded-lg bg-elevated/60 p-3 sm:p-4"
      >
        <NuxtLink
          :to="serverPath(s)"
          class="group flex min-w-0 flex-1 items-center gap-4 rounded-md focus-visible:outline-3 outline-primary/40"
        >
          <div class="size-12 shrink-0 overflow-hidden rounded-lg sm:size-14">
            <MediaImage
              :src="s.serverAvatarUrl"
              :alt="s.servername"
              fallback-icon="i-lucide-server"
            />
          </div>
          <div class="min-w-0">
            <p class="truncate font-semibold text-highlighted group-hover:underline">
              {{ s.servername }}
            </p>
            <p class="flex flex-wrap items-center gap-x-3 text-sm text-muted">
              <span class="truncate">{{ t('server.by', { username: s.username }) }}</span>
              <span
                class="flex items-center gap-1"
                :aria-label="t('server.stars', { n: n(s.serverTotalStar) }, s.serverTotalStar)"
              >
                <UIcon
                  name="i-lucide-star"
                  class="size-3.5"
                />
                {{ n(s.serverTotalStar) }}
              </span>
            </p>
          </div>
        </NuxtLink>
        <UButton
          icon="i-lucide-star-off"
          :label="t('server.unstar')"
          color="neutral"
          variant="soft"
          :loading="pending.has(`${s.username}/${s.servername}`)"
          :ui="{ label: 'hidden sm:inline' }"
          :aria-label="`${t('server.unstar')} ${s.servername}`"
          @click="unstar(s)"
        />
      </li>
    </ul>

    <UPagination
      v-if="data && data.totalPages > 1"
      v-model:page="page"
      :total="data.totalElements"
      :items-per-page="PAGE_SIZE"
      class="mt-6 flex justify-center"
    />
  </UContainer>
</template>
