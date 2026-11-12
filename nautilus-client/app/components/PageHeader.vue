<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
    <div class="min-w-0">
      <h1 class="font-pixel text-pixel-3xl text-highlighted">
        {{ title }}
      </h1>
      <p
        v-if="description"
        class="mt-1 text-muted"
      >
        {{ description }}
      </p>
      <slot name="meta" />
    </div>
    <div
      v-if="$slots.actions"
      class="flex shrink-0 flex-wrap gap-2"
    >
      <slot name="actions" />
    </div>
  </div>
</template>
