<script setup lang="ts">
import type { GalleryImage } from '~/types/api'

const props = defineProps<{
  images: GalleryImage[]
  /** Show only the first N thumbnails; the lightbox still pages through all images */
  limit?: number
}>()

const { t } = useI18n()
const open = ref(false)
const startIndex = ref(0)

function show(index: number) {
  startIndex.value = index
  open.value = true
}

const slides = computed(() => props.images)
const thumbnails = computed(() => props.limit ? props.images.slice(0, props.limit) : props.images)
</script>

<template>
  <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
    <button
      v-for="(image, index) in thumbnails"
      :key="image.imageId"
      type="button"
      class="aspect-video overflow-hidden rounded-lg border-2 border-(--block-border) block-lift lift-secondary"
      :aria-label="image.imageTitle ?? t('gallery.imageN', { n: index + 1 })"
      @click="show(index)"
    >
      <MediaImage
        :src="image.imageUrl"
        :alt="image.imageTitle ?? ''"
      />
    </button>
  </div>

  <UModal
    v-model:open="open"
    :title="t('gallery.title')"
    fullscreen
    :ui="{ body: 'flex items-center' }"
  >
    <template #body>
      <UCarousel
        v-slot="{ item }"
        :items="slides"
        :start-index="startIndex"
        arrows
        dots
        class="w-full"
        :ui="{ item: 'basis-full' }"
      >
        <figure class="flex flex-col items-center gap-3">
          <NuxtImg
            :src="item.imageUrl"
            :alt="item.imageTitle ?? ''"
            class="max-h-[75vh] w-auto rounded-lg border-2 border-(--block-border) object-contain"
          />
          <figcaption
            v-if="item.imageTitle"
            class="text-sm text-muted"
          >
            {{ item.imageTitle }}
          </figcaption>
        </figure>
      </UCarousel>
    </template>
  </UModal>
</template>
