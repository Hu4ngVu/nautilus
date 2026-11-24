<script setup lang="ts">
import type { TabsItem } from '@nuxt/ui'

const { t, n } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const servers = useServerApi()
const { messageFor } = useApiError()

const username = computed(() => String(route.params.username))
const servername = computed(() => String(route.params.servername))
const key = computed(() => `${username.value}/${servername.value}`)

// The server renders as a guest, so a private server only loads once the session is restored
const refetchOnLogin = [() => auth.username]

const { data: server, error, status } = useAsyncData(
  () => `server:${key.value}`,
  () => servers.get(username.value, servername.value),
  { watch: refetchOnLogin }
)

const { canEdit, resolved } = useServerPermission(username, servername)

// Secondary sections load independently so the page renders as soon as the server does
const GALLERY_PAGE_SIZE = 24
const galleryPage = ref(1)
const { data: gallery } = useAsyncData(
  () => `gallery:${key.value}:${galleryPage.value}`,
  () => servers.gallery(username.value, servername.value, { page: galleryPage.value - 1, size: GALLERY_PAGE_SIZE }).catch(() => null),
  { lazy: true, watch: refetchOnLogin }
)

const { data: java } = useAsyncData(
  () => `java:${key.value}`,
  () => servers.javaVersions(username.value, servername.value).catch(() => null),
  { lazy: true, watch: refetchOnLogin }
)

const { data: bedrock } = useAsyncData(
  () => `bedrock:${key.value}`,
  () => servers.bedrockVersions(username.value, servername.value).catch(() => null),
  { lazy: true, watch: refetchOnLogin }
)

// Only owners and editors may list stargazers, so never request it for anyone else
const STARGAZER_PAGE_SIZE = 24
const stargazerPage = ref(1)
const { data: stargazers, execute: loadStargazers } = useAsyncData(
  () => `stargazers:${key.value}:${stargazerPage.value}`,
  () => servers.stargazers(username.value, servername.value, { page: stargazerPage.value - 1, size: STARGAZER_PAGE_SIZE }).catch(() => null),
  { immediate: false, watch: [stargazerPage] }
)
watch(canEdit, (allowed) => {
  if (allowed) loadStargazers()
}, { immediate: true })

type Tab = 'about' | 'gallery' | 'stargazers'

const tab = computed<Tab>({
  get: () => {
    const q = route.query.tab
    return q === 'gallery' || q === 'stargazers' ? q : 'about'
  },
  set: (value) => {
    router.replace({ query: { ...route.query, tab: value === 'about' ? undefined : value } })
  }
})

// A stargazers link opened without permission falls back once permission is known
watch([tab, canEdit, resolved], ([current, allowed, known]) => {
  if (current === 'stargazers' && known && !allowed) tab.value = 'about'
}, { immediate: true })

const galleryCount = computed(() => gallery.value?.page.totalElements ?? 0)

// The About tab previews the first gallery page, so leaving the Gallery tab rewinds it
watch(tab, (current) => {
  if (current === 'about') galleryPage.value = 1
})

const tabs = computed<TabsItem[]>(() => [
  { label: t('server.tabs.about'), value: 'about', slot: 'about' as const },
  { label: t('server.tabs.gallery'), value: 'gallery', slot: 'gallery' as const, badge: galleryCount.value ? n(galleryCount.value) : undefined },
  ...(canEdit.value
    ? [{ label: t('server.tabs.stargazers'), value: 'stargazers', slot: 'stargazers' as const, badge: n(server.value?.starCount ?? 0) }]
    : [])
])

const starCount = computed({
  get: () => server.value?.starCount ?? 0,
  set: (v) => {
    if (server.value) server.value.starCount = v
  }
})

/** Tags grouped by category; the detail response carries tags and categories as parallel lists */
const tagGroups = computed(() => {
  const s = server.value
  if (!s?.tags?.length) return []
  const groups = new Map<string, string[]>()
  s.tags.forEach((tag, i) => {
    const category = s.categories?.[i] ?? 'OTHER'
    groups.set(category, [...(groups.get(category) ?? []), tag])
  })
  return [...groups.entries()]
})

const versionRows = computed(() => {
  const rows: { platform: string, primary: string | null, all: string[] }[] = []
  const javaAll = java.value ? Object.values(java.value.javaVersions ?? {}).flat() : server.value?.javaVersions ?? []
  const bedrockAll = bedrock.value ? Object.values(bedrock.value.bedrockVersions ?? {}).flat() : server.value?.bedrockVersions ?? []
  if (javaAll.length) rows.push({ platform: 'Java', primary: java.value?.primaryJavaVersion ?? null, all: javaAll })
  if (bedrockAll.length) rows.push({ platform: 'Bedrock', primary: bedrock.value?.primaryBedrockVersion ?? null, all: bedrockAll })
  return rows
})

const managePath = computed(() => `${serverPath(username.value, servername.value)}/manage`)

useSeoMeta({
  title: () => server.value?.servername ?? servername.value,
  description: () => server.value?.summary,
  ogImage: () => server.value?.bannerUrl ?? server.value?.avatarUrl ?? undefined
})
</script>

<template>
  <div>
    <UContainer
      v-if="error"
      class="py-12"
    >
      <EmptyState
        icon="i-lucide-server-off"
        :title="t('server.notFound')"
        :description="messageFor(error)"
      >
        <UButton
          to="/browse"
          :label="t('nav.backToBrowse')"
          color="neutral"
          variant="soft"
          class="mt-2"
        />
      </EmptyState>
    </UContainer>

    <template v-else-if="server">
      <div class="relative aspect-[4/1] max-h-80 min-h-32 w-full overflow-hidden border-b-2 border-(--block-border)">
        <MediaImage
          :src="server.bannerUrl"
          :alt="t('server.banner')"
          :name="server.servername"
          banner
          eager
        />
        <div
          class="pointer-events-none absolute inset-0 bg-gradient-to-b from-transparent from-40% to-(--ui-bg)"
          aria-hidden="true"
        />
      </div>

      <UContainer class="pb-16">
        <div class="relative -mt-12 flex flex-col gap-4 sm:-mt-14 sm:flex-row sm:items-end">
          <div class="size-24 shrink-0 overflow-hidden rounded-lg border-4 border-(--ui-bg) shadow-[6px_6px_0_var(--color-nautilus-500)] outline-2 outline-(--block-border) sm:size-28">
            <MediaImage
              :src="server.avatarUrl"
              :alt="server.servername"
              :name="server.servername"
              eager
            />
          </div>
          <div class="min-w-0 flex-1">
            <h1 class="truncate font-pixel text-pixel-3xl text-highlighted sm:text-pixel-4xl">
              {{ server.servername }}
            </h1>
            <p class="text-sm text-muted">
              <i18n-t keypath="server.byLink">
                <template #username>
                  <ULink
                    :to="`/${encodeURIComponent(username)}`"
                    class="font-medium text-secondary"
                  >
                    {{ username }}
                  </ULink>
                </template>
              </i18n-t>
            </p>
          </div>
          <div class="flex flex-wrap gap-2">
            <StarButton
              v-model:count="starCount"
              :username="username"
              :servername="server.servername"
            />
            <UButton
              v-if="canEdit"
              :to="managePath"
              icon="i-lucide-settings"
              :label="t('server.manage')"
              color="neutral"
              variant="soft"
            />
          </div>
        </div>

        <div class="mt-4 flex flex-wrap gap-2">
          <UBadge
            v-if="server.serverPlatform"
            :label="PLATFORM_LABEL[server.serverPlatform]"
            color="secondary"
            variant="solid"
          />
          <UBadge
            v-if="server.serverType"
            :label="t(`server.type.${server.serverType}`)"
            color="neutral"
            variant="soft"
          />
          <UBadge
            v-if="server.requiresPremium"
            :label="t('server.premium')"
            icon="i-lucide-badge-check"
            color="primary"
            variant="solid"
          />
          <UBadge
            v-if="!server.visibility"
            :label="t('server.private')"
            icon="i-lucide-lock"
            color="warning"
            variant="solid"
          />
        </div>

        <div class="mt-8 grid gap-8 lg:grid-cols-[1fr_20rem]">
          <UTabs
            v-model="tab"
            :items="tabs"
            variant="link"
            :unmount-on-hide="false"
            class="min-w-0"
            :ui="{ list: 'mb-6', trailingBadge: 'font-sans' }"
          >
            <template #about>
              <div class="flex flex-col gap-8">
                <p
                  v-if="server.summary"
                  class="text-lg text-toned"
                >
                  {{ server.summary }}
                </p>

                <MarkdownRender
                  v-if="server.bio"
                  :source="server.bio"
                />

                <section
                  v-if="gallery?.content.length"
                  class="flex flex-col gap-3"
                >
                  <div class="flex items-baseline justify-between gap-4">
                    <h2 class="font-pixel text-pixel-xl text-highlighted">
                      {{ t('gallery.title') }}
                    </h2>
                    <UButton
                      v-if="galleryCount > 3"
                      :label="t('gallery.seeAll', { n: n(galleryCount) })"
                      trailing-icon="i-lucide-arrow-right"
                      color="secondary"
                      variant="link"
                      @click="tab = 'gallery'"
                    />
                  </div>
                  <GalleryGrid
                    :images="gallery.content"
                    :limit="3"
                  />
                </section>
              </div>
            </template>

            <template #gallery>
              <EmptyState
                v-if="!gallery?.content.length"
                icon="i-lucide-images"
                :title="t('gallery.empty')"
              />
              <template v-else>
                <GalleryGrid :images="gallery.content" />
                <UPagination
                  v-if="gallery.page.totalPages > 1"
                  v-model:page="galleryPage"
                  :total="gallery.page.totalElements"
                  :items-per-page="GALLERY_PAGE_SIZE"
                  class="mt-6 flex justify-center"
                />
              </template>
            </template>

            <template #stargazers>
              <EmptyState
                v-if="!stargazers?.content.length"
                icon="i-lucide-star"
                :title="t('server.noStargazers')"
              />
              <template v-else>
                <ul class="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">
                  <li
                    v-for="u in stargazers.content"
                    :key="u.username"
                  >
                    <ULink
                      :to="`/${encodeURIComponent(u.username)}`"
                      class="flex items-center gap-3 p-3 block-surface block-lift lift-secondary"
                    >
                      <UAvatar
                        :src="u.avatarUrl ?? undefined"
                        :alt="u.username"
                      />
                      <span class="min-w-0 truncate font-medium text-highlighted">{{ u.username }}</span>
                    </ULink>
                  </li>
                </ul>
                <UPagination
                  v-if="stargazers.page.totalPages > 1"
                  v-model:page="stargazerPage"
                  :total="stargazers.page.totalElements"
                  :items-per-page="STARGAZER_PAGE_SIZE"
                  class="mt-6 flex justify-center"
                />
              </template>
            </template>
          </UTabs>

          <aside class="flex flex-col gap-5 lg:sticky lg:top-[calc(var(--ui-header-height)+1.5rem)] lg:self-start">
            <section
              v-if="server.javaAddress || server.bedrockAddress"
              class="flex flex-col gap-2 p-4 block-surface border-nautilus-500! block-raised lift-primary"
            >
              <h2 class="mb-1 font-pixel text-pixel-xl text-highlighted">
                {{ t('server.join') }}
              </h2>
              <CopyAddress
                v-if="server.javaAddress"
                :label="t('server.javaAddress')"
                :address="server.javaAddress"
              />
              <CopyAddress
                v-if="server.bedrockAddress"
                :label="t('server.bedrockAddress')"
                :address="server.bedrockAddress"
              />
            </section>

            <section
              v-if="versionRows.length"
              class="p-4 block-surface"
            >
              <h2 class="mb-3 font-pixel text-pixel-lg text-highlighted">
                {{ t('server.versions') }}
              </h2>
              <div
                v-for="row in versionRows"
                :key="row.platform"
                class="mb-3 last:mb-0"
              >
                <p class="mb-1 text-xs text-muted">
                  {{ row.platform }}
                </p>
                <div class="flex flex-wrap gap-1">
                  <UBadge
                    v-for="v in row.all"
                    :key="v"
                    :label="v"
                    :color="v === row.primary ? 'primary' : 'neutral'"
                    :variant="v === row.primary ? 'solid' : 'soft'"
                    :class="{ 'font-semibold': v === row.primary }"
                    size="sm"
                  />
                </div>
              </div>
            </section>

            <section
              v-if="tagGroups.length"
              class="p-4 block-surface"
            >
              <h2 class="mb-3 font-pixel text-pixel-lg text-highlighted">
                {{ t('server.tags') }}
              </h2>
              <div
                v-for="[category, tags] in tagGroups"
                :key="category"
                class="mb-3 last:mb-0"
              >
                <p class="mb-1 text-xs text-muted">
                  {{ t(`tags.category.${category}`, category) }}
                </p>
                <div class="flex flex-wrap gap-1">
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
            </section>
          </aside>
        </div>
      </UContainer>
    </template>

    <UContainer
      v-else-if="status === 'pending'"
      class="py-8"
    >
      <USkeleton class="aspect-[4/1] max-h-80 w-full" />
      <USkeleton class="mt-6 h-10 w-1/3" />
      <USkeleton class="mt-3 h-4 w-2/3" />
    </UContainer>
  </div>
</template>
