<script setup lang="ts">
const { t, n } = useI18n()
const route = useRoute()
const auth = useAuthStore()
const users = useUserApi()
const servers = useServerApi()
const { messageFor } = useApiError()

const username = computed(() => String(route.params.username))
const isMe = computed(() => auth.username === username.value)

const { data: user, error } = useAsyncData(
  () => `user:${username.value}`,
  () => users.get(username.value)
)

const serverPage = ref(1)
const PAGE_SIZE = 12

const { data: owned, status: ownedStatus } = useAsyncData(
  () => `user-servers:${username.value}:${serverPage.value}`,
  () => servers.listByUser(username.value, { page: serverPage.value - 1, size: PAGE_SIZE }),
  { lazy: true }
)

const serverPath = (servername: string) => `/s/${encodeURIComponent(username.value)}/${encodeURIComponent(servername)}`

useSeoMeta({
  title: () => username.value,
  description: () => user.value?.bio?.slice(0, 160)
})
</script>

<template>
  <UContainer class="py-8">
    <EmptyState
      v-if="error"
      icon="i-lucide-user-x"
      :title="t('profile.notFound')"
      :description="messageFor(error)"
    />

    <div
      v-else
      class="grid gap-8 md:grid-cols-[16rem_1fr] lg:grid-cols-[18rem_1fr]"
    >
      <!-- Profile card, GitHub-style left column -->
      <aside class="flex flex-col gap-4">
        <div class="flex items-center gap-4 md:flex-col md:items-start">
          <UAvatar
            :src="user?.avatarUrl ?? undefined"
            :alt="username"
            class="size-20 text-3xl md:size-auto md:aspect-square md:w-full md:text-6xl"
          />
          <div class="min-w-0">
            <h1 class="truncate text-2xl font-bold text-highlighted">
              {{ username }}
            </h1>
            <p
              v-if="user"
              class="flex items-center gap-1.5 text-sm text-muted"
            >
              <UIcon
                name="i-lucide-server"
                class="size-4"
              />
              {{ t('profile.serverCount', { n: n(user.serverCount) }, user.serverCount) }}
            </p>
          </div>
        </div>

        <UButton
          v-if="isMe"
          to="/settings"
          icon="i-lucide-pencil"
          :label="t('profile.edit')"
          color="neutral"
          variant="soft"
          block
        />

        <div
          v-if="user?.bio"
          class="rounded-lg bg-elevated/50 p-4"
        >
          <MarkdownRender :source="user.bio" />
        </div>
      </aside>

      <!-- Server cards, GitHub repository-card style -->
      <section class="min-w-0">
        <h2 class="mb-4 font-semibold text-highlighted">
          {{ t('profile.servers') }}
        </h2>

        <EmptyState
          v-if="ownedStatus === 'success' && !owned?.content.length"
          icon="i-lucide-server"
          :title="t('profile.noServers')"
        >
          <UButton
            v-if="isMe"
            to="/new"
            icon="i-lucide-plus"
            :label="t('nav.newServer')"
            class="mt-2"
          />
        </EmptyState>

        <div
          v-else
          class="grid gap-4 sm:grid-cols-2"
        >
          <template v-if="ownedStatus === 'pending' && !owned">
            <USkeleton
              v-for="i in 4"
              :key="i"
              class="h-32 rounded-lg"
            />
          </template>
          <NuxtLink
            v-for="s in owned?.content ?? []"
            v-else
            :key="s.servername"
            :to="serverPath(s.servername)"
            class="group flex flex-col gap-2 rounded-lg bg-elevated/60 p-4 transition-colors hover:bg-elevated focus-visible:outline-3 outline-primary/40"
          >
            <div class="flex min-w-0 items-center gap-2">
              <div class="size-6 shrink-0 overflow-hidden rounded">
                <MediaImage
                  :src="s.avatarUrl"
                  :alt="s.servername"
                  fallback-icon="i-lucide-server"
                />
              </div>
              <span class="truncate font-semibold text-secondary group-hover:underline">{{ s.servername }}</span>
              <UBadge
                v-if="s.requiresPremium"
                :label="t('server.premium')"
                color="neutral"
                variant="soft"
                size="sm"
                class="ms-auto shrink-0"
              />
            </div>
            <p class="line-clamp-2 flex-1 text-sm text-toned">
              {{ s.summary }}
            </p>
            <p
              class="flex items-center gap-1 text-sm text-muted"
              :aria-label="t('server.stars', { n: n(s.starCount) }, s.starCount)"
            >
              <UIcon
                name="i-lucide-star"
                class="size-4"
              />
              {{ n(s.starCount) }}
            </p>
          </NuxtLink>
        </div>

        <UPagination
          v-if="owned && owned.totalPages > 1"
          v-model:page="serverPage"
          :total="owned.totalElements"
          :items-per-page="PAGE_SIZE"
          class="mt-6 flex justify-center"
        />
      </section>
    </div>
  </UContainer>
</template>
