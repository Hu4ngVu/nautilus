<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { t } = useI18n()
const route = useRoute()
const servers = useServerApi()
const { messageFor } = useApiError()

const username = computed(() => String(route.params.username))
const servername = computed(() => String(route.params.servername))
const { isOwner, canEdit } = useServerPermission(username, servername)

const { data: server, error, refresh } = useAsyncData(
  () => `manage:${username.value}/${servername.value}`,
  () => servers.get(username.value, servername.value)
)

const serverPath = computed(() => `/s/${encodeURIComponent(username.value)}/${encodeURIComponent(servername.value)}`)

const tabs = computed(() => [
  { label: t('manage.tabs.general'), value: 'general', icon: 'i-lucide-info', slot: 'general' as const },
  { label: t('manage.tabs.gallery'), value: 'gallery', icon: 'i-lucide-images', slot: 'gallery' as const },
  { label: t('manage.tabs.tags'), value: 'tags', icon: 'i-lucide-tags', slot: 'tags' as const },
  { label: t('manage.tabs.versions'), value: 'versions', icon: 'i-lucide-git-branch', slot: 'versions' as const },
  ...(isOwner.value ? [{ label: t('manage.tabs.advanced'), value: 'advanced', icon: 'i-lucide-shield', slot: 'advanced' as const }] : [])
])

const tab = computed({
  get: () => typeof route.query.tab === 'string' ? route.query.tab : 'general',
  set: v => navigateTo({ query: { ...route.query, tab: v } }, { replace: true })
})

async function onRenamed(newName: string) {
  await navigateTo(`/s/${encodeURIComponent(username.value)}/${encodeURIComponent(newName)}/manage?tab=advanced`, { replace: true })
}

useSeoMeta({ title: () => t('manage.title', { name: servername.value }) })
</script>

<template>
  <UContainer class="max-w-4xl py-8">
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <div class="min-w-0">
        <ULink
          :to="serverPath"
          class="inline-flex items-center gap-1 text-sm text-muted"
        >
          <UIcon
            name="i-lucide-arrow-left"
            class="size-4"
          />
          {{ t('manage.back') }}
        </ULink>
        <h1 class="truncate text-2xl font-bold text-highlighted">
          {{ t('manage.title', { name: servername }) }}
        </h1>
      </div>
    </div>

    <EmptyState
      v-if="error"
      icon="i-lucide-server-off"
      :title="t('server.notFound')"
      :description="messageFor(error)"
    />

    <UTabs
      v-else-if="server"
      v-model="tab"
      :items="tabs"
      variant="link"
      :ui="{ list: 'overflow-x-auto', content: 'pt-6' }"
    >
      <template #general>
        <ManageGeneral
          :key="`${servername}-${server.avatarUrl}-${server.bannerUrl}`"
          :username="username"
          :server="server"
          @saved="refresh()"
        />
      </template>
      <template #gallery>
        <ManageGallery
          :username="username"
          :servername="servername"
        />
      </template>
      <template #tags>
        <ManageTags
          :username="username"
          :servername="servername"
        />
      </template>
      <template #versions>
        <ManageVersions
          :username="username"
          :servername="servername"
        />
      </template>
      <template #advanced>
        <ManageAdvanced
          :username="username"
          :server="server"
          @saved="refresh()"
          @renamed="onRenamed"
        />
      </template>
    </UTabs>

    <UAlert
      v-if="server && !canEdit"
      class="mt-6"
      color="warning"
      variant="soft"
      icon="i-lucide-lock"
      :title="t('manage.noPermission')"
    />
  </UContainer>
</template>
