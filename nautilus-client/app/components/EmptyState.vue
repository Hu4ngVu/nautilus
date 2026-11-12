<script setup lang="ts">
defineProps<{
  icon?: string
  title: string
  description?: string
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-2 rounded-lg bg-elevated/50 px-6 py-12 text-center">
    <UIcon
      :name="icon ?? 'i-lucide-inbox'"
      class="size-10 text-dimmed"
    />
    <p class="font-medium text-highlighted">
      {{ title }}
    </p>
    <p
      v-if="description"
      class="text-sm text-muted"
    >
      {{ description }}
    </p>
    <slot />
  </div>
</template>
