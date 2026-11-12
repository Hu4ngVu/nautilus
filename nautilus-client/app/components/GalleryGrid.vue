<script setup lang="ts">
import type { GalleryImage } from '~/types/api'

const props = defineProps<{ images: GalleryImage[] }>()

const { t } = useI18n()
const open = ref(false)
const startIndex = ref(0)

function show(index: number) {
  startIndex.value = index
  open.value = true
}

const slides = computed(() => props.images)
</script>

<template>
  <div class="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-4">
    <button
      v-for="(image, index) in images"
      :key="image.imageId"
      type="button"
      class="aspect-video overflow-hidden rounded-md focus-visible:outline-3 outline-primary/40"
      :aria-label="image.imageTitle ?? t('gallery.imageN', { n: index + 1 })"
      @click="show(index)"
    >
      <MediaImage
        :src="image.imageUrl"
        :alt="image.imageTitle ?? ''"
        class="transition-transform hover:scale-105"
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
            class="max-h-[75vh] w-auto rounded-md object-contain"
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
