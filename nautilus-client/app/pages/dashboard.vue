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
    <PageHeader
      :title="t('dashboard.title')"
      :description="t('dashboard.subtitle')"
    >
      <template
        v-if="data"
        #meta
      >
        <UBadge
          :label="t('profile.serverCount', { n: n(data.totalElements) }, data.totalElements)"
          icon="i-lucide-server"
          color="neutral"
          variant="soft"
          class="mt-3"
        />
      </template>
      <template #actions>
        <UButton
          to="/new"
          icon="i-lucide-plus"
          :label="t('nav.newServer')"
        />
      </template>
    </PageHeader>

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
      class="grid gap-5 sm:grid-cols-2 xl:grid-cols-3"
    >
      <template v-if="status === 'pending' && !data">
        <USkeleton
          v-for="i in 6"
          :key="i"
          class="h-64"
        />
      </template>
      <template v-else>
        <ServerTile
          v-for="(s, i) in data?.content ?? []"
          :key="s.servername"
          :server="s"
          :index="i"
          show-visibility
        >
          <template #actions>
            <UButton
              :to="serverPath(s.username, s.servername)"
              icon="i-lucide-eye"
              :label="t('dashboard.view')"
              color="neutral"
              variant="soft"
              size="sm"
            />
            <UButton
              :to="`${serverPath(s.username, s.servername)}/manage`"
              icon="i-lucide-pencil"
              :label="t('common.edit')"
              color="neutral"
              variant="soft"
              size="sm"
            />
            <UButton
              icon="i-lucide-trash-2"
              :label="t('common.delete')"
              :aria-label="`${t('common.delete')} ${s.servername}`"
              color="error"
              variant="ghost"
              size="sm"
              class="ms-auto"
              @click="deleting = s"
            />
          </template>
        </ServerTile>
        <NuxtLink
          v-if="page === 1"
          to="/new"
          class="flex min-h-64 flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed border-(--block-border) font-pixel text-pixel-lg text-muted transition-colors hover:border-nautilus-500 hover:text-primary"
        >
          <UIcon
            name="i-lucide-plus"
            class="size-8"
          />
          {{ t('nav.newServer') }}
        </NuxtLink>
      </template>
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
