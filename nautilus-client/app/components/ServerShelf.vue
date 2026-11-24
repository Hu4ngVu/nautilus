<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'
import type { ServerFilters } from '~/types/api'

// A titled row of top servers for one filter; hides itself when there is nothing to show
const props = defineProps<{
  title: string
  filters: ServerFilters & { sort?: string }
  to: RouteLocationRaw
}>()

const { t } = useI18n()
const servers = useServerApi()

const { data, status } = useAsyncData(
  `shelf:${JSON.stringify(props.filters)}`,
  () => servers.list({ sort: 'starCount,desc', ...props.filters, page: 0, size: 8 })
)

const visible = computed(() => status.value === 'pending' || !!data.value?.content.length)
</script>

<template>
  <section
    v-if="visible && status !== 'error'"
    class="flex flex-col gap-3"
  >
    <div class="flex items-baseline justify-between gap-4">
      <h2 class="font-pixel text-pixel-2xl text-highlighted">
        {{ title }}
      </h2>
      <ULink
        :to="to"
        class="inline-flex shrink-0 items-center gap-1 font-pixel text-pixel-base text-secondary hover:underline"
      >
        {{ t('home.seeAll') }}
        <UIcon
          name="i-lucide-arrow-right"
          class="size-4"
        />
      </ULink>
    </div>

    <!-- Mobile: horizontal snap strip; sm+: grid. Padding leaves room for the lift shadow. -->
    <div class="-mx-4 flex snap-x snap-mandatory gap-4 overflow-x-auto px-4 pt-2 pb-4 sm:mx-0 sm:grid sm:grid-cols-2 sm:overflow-visible sm:px-0 lg:grid-cols-4">
      <template v-if="status === 'pending' && !data">
        <USkeleton
          v-for="i in 4"
          :key="i"
          class="h-60 w-64 shrink-0 sm:w-auto"
        />
      </template>
      <div
        v-for="(s, i) in data?.content ?? []"
        v-else
        :key="`${s.username}/${s.servername}`"
        class="w-64 shrink-0 snap-start sm:w-auto"
      >
        <ServerTile
          :server="s"
          :index="i"
        />
      </div>
    </div>
  </section>
</template>
