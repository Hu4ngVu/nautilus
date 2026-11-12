<script setup lang="ts">
import type { ServerSummary } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { t, n } = useI18n()
const auth = useAuthStore()
const servers = useServerApi()
const { messageFor, notify, success } = useApiError()

useSeoMeta({ title: () => t('dashboard.title') })

const PAGE_SIZE = 12
const page = ref(1)

const { data, status, error, refresh } = useAsyncData(
  () => `dashboard:${auth.username}:${page.value}`,
  () => servers.listByUser(auth.username!, { page: page.value - 1, size: PAGE_SIZE })
)

const serverPath = (s: ServerSummary) => `/s/${encodeURIComponent(s.username)}/${encodeURIComponent(s.servername)}`

const deleting = ref<ServerSummary | null>(null)
const removing = ref(false)

const deleteOpen = computed({
  get: () => !!deleting.value,
  set: (open) => {
    if (!open) deleting.value = null
  }
})

async function confirmDelete() {
  const target = deleting.value
  if (!target) return
  removing.value = true
  try {
    await servers.remove(target.username, target.servername)
    success(t('manage.deleted'))
    deleting.value = null
    // Step back if the last server on this page was removed
    if (data.value?.content.length === 1 && page.value > 1) page.value--
    else await refresh()
  } catch (err) {
    notify(err)
  } finally {
    removing.value = false
  }
}
</script>

<template>
  <UContainer class="py-8">
    <div class="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-highlighted sm:text-3xl">
          {{ t('dashboard.title') }}
        </h1>
        <p class="mt-1 text-muted">
          {{ t('dashboard.subtitle') }}
        </p>
      </div>
      <UButton
        to="/new"
        icon="i-lucide-plus"
        :label="t('nav.newServer')"
        class="self-start sm:self-auto"
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
      icon="i-lucide-server"
      :title="t('dashboard.empty')"
      :description="t('dashboard.emptyHint')"
    >
      <UButton
        to="/new"
        icon="i-lucide-plus"
        :label="t('nav.newServer')"
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
          class="h-40 rounded-lg"
        />
      </template>
      <article
        v-for="s in data?.content ?? []"
        v-else
        :key="s.servername"
        class="flex flex-col gap-4 rounded-lg bg-elevated/60 p-4"
      >
        <NuxtLink
          :to="serverPath(s)"
          class="group flex min-w-0 gap-3 rounded-md focus-visible:outline-3 outline-primary/40"
        >
          <div class="size-14 shrink-0 overflow-hidden rounded-lg">
            <MediaImage
              :src="s.avatarUrl"
              :alt="s.servername"
              fallback-icon="i-lucide-server"
            />
          </div>
          <div class="min-w-0 flex-1">
            <h2 class="truncate font-semibold text-highlighted group-hover:underline">
              {{ s.servername }}
            </h2>
            <p
              class="mt-0.5 flex items-center gap-1 text-sm text-muted"
              :aria-label="t('server.stars', { n: n(s.starCount) }, s.starCount)"
            >
              <UIcon
                name="i-lucide-star"
                class="size-4"
              />
              {{ n(s.starCount) }}
            </p>
            <p
              v-if="s.summary"
              class="mt-1 line-clamp-2 text-sm text-toned"
            >
              {{ s.summary }}
            </p>
          </div>
        </NuxtLink>
        <div class="mt-auto flex flex-wrap gap-2">
          <UButton
            :to="serverPath(s)"
            icon="i-lucide-eye"
            :label="t('dashboard.view')"
            color="neutral"
            variant="soft"
            size="sm"
          />
          <UButton
            :to="`${serverPath(s)}/manage`"
            icon="i-lucide-pencil"
            :label="t('common.edit')"
            color="neutral"
            variant="soft"
            size="sm"
          />
          <UButton
            icon="i-lucide-trash-2"
            :label="t('common.delete')"
            color="error"
            variant="ghost"
            size="sm"
            class="ms-auto"
            @click="deleting = s"
          />
        </div>
      </article>
    </div>

    <UPagination
      v-if="data && data.totalPages > 1"
      v-model:page="page"
      :total="data.totalElements"
      :items-per-page="PAGE_SIZE"
      class="mt-6 flex justify-center"
    />

    <UModal
      v-model:open="deleteOpen"
      :title="t('manage.deleteTitle')"
      :description="deleting ? t('dashboard.deleteConfirm', { name: deleting.servername }) : undefined"
    >
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton
            :label="t('common.cancel')"
            color="neutral"
            variant="soft"
            :disabled="removing"
            @click="deleting = null"
          />
          <UButton
            :label="t('common.delete')"
            icon="i-lucide-trash-2"
            color="error"
            :loading="removing"
            @click="confirmDelete"
          />
        </div>
      </template>
    </UModal>
  </UContainer>
</template>
