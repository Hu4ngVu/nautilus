<script setup lang="ts">
defineProps<{
  icon?: string
  title: string
  description?: string
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed border-(--block-border) bg-(--block-surface)/60 px-6 py-12 text-center">
    <div class="mb-2 flex size-14 items-center justify-center rounded-md bg-elevated shadow-[inset_-4px_-4px_0_var(--ui-border-accented)]">
      <UIcon
        :name="icon ?? 'i-lucide-inbox'"
        class="size-7 text-muted"
      />
    </div>
    <p class="font-pixel text-pixel-lg text-highlighted">
      {{ title }}
    </p>
    <p
      v-if="description"
      class="max-w-md text-sm text-muted"
    >
      {{ description }}
    </p>
    <slot />
  </div>
</template>
