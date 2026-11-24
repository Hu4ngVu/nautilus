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

const serverPath = computed(() => `/${encodeURIComponent(username.value)}/${encodeURIComponent(servername.value)}`)

const tabs = computed(() => [
  { label: t('manage.tabs.general'), value: 'general', icon: 'i-lucide-info', slot: 'general' as const },
  { label: t('manage.tabs.gallery'), value: 'gallery', icon: 'i-lucide-images', slot: 'gallery' as const },
  { label: t('manage.tabs.tags'), value: 'tags', icon: 'i-lucide-tags', slot: 'tags' as const },
  { label: t('manage.tabs.versions'), value: 'versions', icon: 'i-lucide-git-branch', slot: 'versions' as const },
  ...(isOwner.value ? [{ label: t('manage.tabs.advanced'), value: 'advanced', icon: 'i-lucide-shield', slot: 'advanced' as const }] : [])
])

const tab = computed({
  get: () => {
    const q = route.query.tab
    return typeof q === 'string' && tabs.value.some(item => item.value === q) ? q : 'general'
  },
  set: v => navigateTo({ query: { ...route.query, tab: v } }, { replace: true })
})

async function onRenamed(newName: string) {
  await navigateTo(`/${encodeURIComponent(username.value)}/${encodeURIComponent(newName)}/manage?tab=advanced`, { replace: true })
}

useSeoMeta({ title: () => t('manage.title', { name: servername.value }) })
</script>

<template>
  <UContainer class="py-8">
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
        <h1 class="mt-1 truncate font-pixel text-pixel-3xl text-highlighted">
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

    <div
      v-else-if="server"
      class="grid gap-8 md:grid-cols-[14rem_1fr]"
    >
      <nav
        :aria-label="t('manage.title', { name: servername })"
        class="-mx-4 flex gap-2 overflow-x-auto px-4 pb-2 md:mx-0 md:flex-col md:self-start md:overflow-visible md:px-0 md:sticky md:top-[calc(var(--ui-header-height)+1.5rem)]"
      >
        <UButton
          v-for="item in tabs"
          :key="item.value"
          :icon="item.icon"
          :label="item.label"
          :aria-current="tab === item.value ? 'page' : undefined"
          :color="tab === item.value ? (item.value === 'advanced' ? 'error' : 'primary') : 'neutral'"
          :variant="tab === item.value ? 'solid' : 'ghost'"
          class="shrink-0 md:w-full"
          @click="tab = item.value"
        />
      </nav>

      <div class="min-w-0 p-5 block-surface sm:p-6">
        <ManageGeneral
          v-if="tab === 'general'"
          :key="`${servername}-${server.avatarUrl}-${server.bannerUrl}`"
          :username="username"
          :server="server"
          @saved="refresh()"
        />
        <ManageGallery
          v-else-if="tab === 'gallery'"
          :username="username"
          :servername="servername"
        />
        <ManageTags
          v-else-if="tab === 'tags'"
          :username="username"
          :servername="servername"
        />
        <ManageVersions
          v-else-if="tab === 'versions'"
          :username="username"
          :servername="servername"
        />
        <ManageAdvanced
          v-else-if="tab === 'advanced'"
          :username="username"
          :server="server"
          @saved="refresh()"
          @renamed="onRenamed"
        />
      </div>
    </div>

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
