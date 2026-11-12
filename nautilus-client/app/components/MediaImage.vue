<script setup lang="ts">
// Thin wrapper over NuxtImg; images (including animated GIFs) are served as-is from object storage
withDefaults(defineProps<{
  src?: string | null
  alt?: string
  fallbackIcon?: string
  eager?: boolean
}>(), {
  src: null,
  alt: '',
  fallbackIcon: 'i-lucide-image',
  eager: false
})
</script>

<template>
  <NuxtImg
    v-if="src"
    :src="src"
    :alt="alt"
    :loading="eager ? 'eager' : 'lazy'"
    decoding="async"
    class="size-full object-cover"
  />
  <div
    v-else
    class="flex size-full items-center justify-center bg-elevated text-dimmed"
    role="img"
    :aria-label="alt"
  >
    <UIcon
      :name="fallbackIcon"
      class="size-1/3 max-h-12 max-w-12"
    />
  </div>
</template>
