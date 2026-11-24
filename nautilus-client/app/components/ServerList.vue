<script setup lang="ts">
import type { ServerSummary } from '~/types/api'

withDefaults(defineProps<{
  servers: ServerSummary[]
  loading?: boolean
  view?: 'list' | 'grid'
}>(), {
  loading: false,
  view: 'list'
})
</script>

<template>
  <div :class="view === 'grid' ? 'grid gap-4 sm:grid-cols-2 xl:grid-cols-3' : 'flex flex-col gap-3'">
    <template v-if="loading && !servers.length">
      <USkeleton
        v-for="i in 6"
        :key="i"
        :class="view === 'grid' ? 'h-64' : 'h-32'"
      />
    </template>
    <template v-else-if="view === 'grid'">
      <ServerTile
        v-for="(s, i) in servers"
        :key="`${s.username}/${s.servername}`"
        :server="s"
        :index="i"
      />
    </template>
    <template v-else>
      <ServerRow
        v-for="(s, i) in servers"
        :key="`${s.username}/${s.servername}`"
        :server="s"
        :index="i"
      />
    </template>
  </div>
</template>
