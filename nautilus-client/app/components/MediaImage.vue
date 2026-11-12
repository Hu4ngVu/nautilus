<script setup lang="ts">
// Thin wrapper over NuxtImg; images (including animated GIFs) are served as-is from object storage.
// Avatars and banners pass `name` and fall back to a BlockAvatar; other images fall back to an icon.
const props = withDefaults(defineProps<{
  src?: string | null
  alt?: string
  name?: string
  banner?: boolean
  fallbackIcon?: string
  eager?: boolean
}>(), {
  src: null,
  alt: '',
  name: undefined,
  banner: false,
  fallbackIcon: 'i-lucide-image',
  eager: false
})

// Signed URLs expire; a failed load drops to the placeholder instead of a broken image
const failed = ref(false)
watch(() => props.src, () => {
  failed.value = false
})
</script>

<template>
  <NuxtImg
    v-if="src && !failed"
    :src="src"
    :alt="alt"
    :loading="eager ? 'eager' : 'lazy'"
    decoding="async"
    class="size-full object-cover"
    @error="failed = true"
  />
  <BlockAvatar
    v-else-if="name"
    :name="name"
    :shape="banner ? 'banner' : 'square'"
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
