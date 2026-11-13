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
      <aside class="flex flex-col gap-4 self-start p-4 block-surface block-raised lift-secondary">
        <div class="flex items-center gap-4 md:flex-col md:items-stretch">
          <UAvatar
            :src="user?.avatarUrl ?? undefined"
            :alt="username"
            class="size-20 border-2 border-(--block-border) font-pixel text-pixel-2xl md:aspect-square md:size-auto md:w-full md:text-[5rem]"
          />
          <div class="min-w-0">
            <h1 class="truncate font-pixel text-pixel-2xl text-highlighted">
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

        <MarkdownRender
          v-if="user?.bio"
          :source="user.bio"
          class="text-sm"
        />

        <UButton
          v-if="isMe"
          to="/settings"
          icon="i-lucide-pencil"
          :label="t('profile.edit')"
          color="neutral"
          variant="soft"
          block
        />
      </aside>

      <!-- Server cards, GitHub repository-card style -->
      <section class="min-w-0">
        <h2 class="mb-4 font-pixel text-pixel-xl text-highlighted">
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
              class="h-32"
            />
          </template>
          <ServerRepoCard
            v-for="(s, i) in owned?.content ?? []"
            v-else
            :key="s.servername"
            :username="s.username"
            :servername="s.servername"
            :avatar-url="s.avatarUrl"
            :star-count="s.starCount"
            :summary="s.summary"
            :platform="s.serverPlatform"
            :index="i"
          />
        </div>

        <UPagination
          v-if="owned && owned.page.totalPages > 1"
          v-model:page="serverPage"
          :total="owned.page.totalElements"
          :items-per-page="PAGE_SIZE"
          class="mt-6 flex justify-center"
        />
      </section>
    </div>
  </UContainer>
</template>
