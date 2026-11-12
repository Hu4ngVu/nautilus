<script setup lang="ts">
const { t } = useI18n()
const route = useRoute()
const servers = useServerApi()
const { messageFor } = useApiError()

const username = computed(() => String(route.params.username))
const servername = computed(() => String(route.params.servername))
const key = computed(() => `${username.value}/${servername.value}`)

const { data: server, error, status } = useAsyncData(
  () => `server:${key.value}`,
  () => servers.get(username.value, servername.value)
)

// Secondary sections load independently so the page renders as soon as the server does
const { data: gallery } = useAsyncData(
  () => `gallery:${key.value}`,
  () => servers.gallery(username.value, servername.value, { size: 24 }).catch(() => null),
  { lazy: true }
)

const { data: java } = useAsyncData(
  () => `java:${key.value}`,
  () => servers.javaVersions(username.value, servername.value).catch(() => null),
  { lazy: true }
)

const { data: bedrock } = useAsyncData(
  () => `bedrock:${key.value}`,
  () => servers.bedrockVersions(username.value, servername.value).catch(() => null),
  { lazy: true }
)

const { data: stargazers } = useAsyncData(
  () => `stargazers:${key.value}`,
  () => servers.stargazers(username.value, servername.value, { size: 20 }).catch(() => null),
  { lazy: true }
)

const { canEdit } = useServerPermission(username, servername)

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
      <div class="aspect-[4/1] max-h-72 w-full overflow-hidden bg-elevated">
        <MediaImage
          :src="server.bannerUrl"
          :alt="t('server.banner')"
          fallback-icon="i-lucide-mountain"
          eager
        />
      </div>

      <UContainer class="pb-12">
        <div class="-mt-10 flex flex-col gap-4 sm:-mt-12 sm:flex-row sm:items-end">
          <div class="size-20 shrink-0 overflow-hidden rounded-lg ring-4 ring-(--ui-bg) sm:size-24">
            <MediaImage
              :src="server.avatarUrl"
              :alt="server.servername"
              fallback-icon="i-lucide-server"
              eager
            />
          </div>
          <div class="min-w-0 flex-1">
            <h1 class="truncate text-2xl font-bold text-highlighted sm:text-3xl">
              {{ server.servername }}
            </h1>
            <p class="text-sm text-muted">
              <i18n-t keypath="server.byLink">
                <template #username>
                  <ULink
                    :to="`/u/${encodeURIComponent(username)}`"
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
              :to="`/s/${encodeURIComponent(username)}/${encodeURIComponent(server.servername)}/manage`"
              icon="i-lucide-settings"
              :label="t('server.manage')"
              color="neutral"
              variant="soft"
            />
          </div>
        </div>

        <div class="mt-4 flex flex-wrap gap-2">
          <UBadge
            :label="server.serverPlatform === 'JAVA' ? 'Java' : 'Bedrock'"
            color="secondary"
            variant="soft"
          />
          <UBadge
            :label="t(`server.type.${server.serverType}`)"
            color="neutral"
            variant="soft"
          />
          <UBadge
            v-if="server.requiresPremium"
            :label="t('server.premium')"
            color="primary"
            variant="soft"
          />
          <UBadge
            v-if="!server.visibility"
            :label="t('server.private')"
            icon="i-lucide-lock"
            color="warning"
            variant="soft"
          />
        </div>

        <div class="mt-8 grid gap-8 lg:grid-cols-3">
          <div class="flex flex-col gap-8 lg:col-span-2">
            <section>
              <p class="text-lg text-toned">
                {{ server.summary }}
              </p>
            </section>

            <section v-if="server.bio">
              <h2 class="mb-3 text-lg font-semibold text-highlighted">
                {{ t('server.about') }}
              </h2>
              <MarkdownRender :source="server.bio" />
            </section>

            <section v-if="gallery?.content.length">
              <h2 class="mb-3 text-lg font-semibold text-highlighted">
                {{ t('gallery.title') }}
              </h2>
              <GalleryGrid :images="gallery.content" />
            </section>
          </div>

          <aside class="flex flex-col gap-6">
            <section
              v-if="server.javaAddress || server.bedrockAddress"
              class="flex flex-col gap-2"
            >
              <h2 class="text-sm font-semibold text-highlighted">
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

            <section v-if="versionRows.length">
              <h2 class="mb-2 text-sm font-semibold text-highlighted">
                {{ t('server.versions') }}
              </h2>
              <div
                v-for="row in versionRows"
                :key="row.platform"
                class="mb-3"
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
                    size="sm"
                  />
                </div>
              </div>
            </section>

            <section v-if="tagGroups.length">
              <h2 class="mb-2 text-sm font-semibold text-highlighted">
                {{ t('server.tags') }}
              </h2>
              <div
                v-for="[category, tags] in tagGroups"
                :key="category"
                class="mb-3"
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

            <section v-if="stargazers?.content.length">
              <h2 class="mb-2 text-sm font-semibold text-highlighted">
                {{ t('server.stargazers') }}
              </h2>
              <UAvatarGroup
                :max="10"
                size="sm"
              >
                <ULink
                  v-for="u in stargazers.content"
                  :key="u.username"
                  :to="`/u/${encodeURIComponent(u.username)}`"
                  :aria-label="u.username"
                >
                  <UAvatar
                    :src="u.avatarUrl ?? undefined"
                    :alt="u.username"
                  />
                </ULink>
              </UAvatarGroup>
            </section>
          </aside>
        </div>
      </UContainer>
    </template>

    <UContainer
      v-else-if="status === 'pending'"
      class="py-8"
    >
      <USkeleton class="aspect-[4/1] max-h-72 w-full rounded-lg" />
      <USkeleton class="mt-6 h-8 w-1/3" />
      <USkeleton class="mt-3 h-4 w-2/3" />
    </UContainer>
  </div>
</template>
