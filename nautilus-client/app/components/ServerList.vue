<script setup lang="ts">
import type { ServerSummary } from '~/types/api'

defineProps<{
  servers: ServerSummary[]
  loading?: boolean
}>()
</script>

<template>
  <div class="flex flex-col gap-3">
    <template v-if="loading && !servers.length">
      <USkeleton
        v-for="i in 6"
        :key="i"
        class="h-32 rounded-lg"
      />
    </template>
    <ServerCard
      v-for="s in servers"
      v-else
      :key="`${s.username}/${s.servername}`"
      v-bind="s"
    />
  </div>
</template>
