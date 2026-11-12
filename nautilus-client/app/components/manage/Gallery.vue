<script setup lang="ts">
import type { GalleryImage } from '~/types/api'

const props = defineProps<{
  username: string
  servername: string
}>()

const { t } = useI18n()
const servers = useServerApi()
const { notify, success } = useApiError()

const { data, refresh, status } = useAsyncData(
  `manage-gallery:${props.username}/${props.servername}`,
  () => servers.gallery(props.username, props.servername, { size: 100 })
)

const images = computed(() => data.value?.content ?? [])

// Add / edit dialog
const editorOpen = ref(false)
const editing = ref<GalleryImage | null>(null)
const draft = reactive({ resourceId: '', title: '', summary: '' })
const saving = ref(false)

async function openNew() {
  editing.value = null
  Object.assign(draft, { resourceId: '', title: '', summary: '' })
  editorOpen.value = true
}

async function openEdit(image: GalleryImage) {
  editing.value = image
  Object.assign(draft, { resourceId: '', title: image.imageTitle ?? '', summary: '' })
  editorOpen.value = true
  try {
    const detail = await servers.galleryImage(props.username, props.servername, image.imageId)
    draft.summary = detail.summary ?? ''
  } catch {
    // Summary is optional; keep the dialog usable
  }
}

async function saveImage() {
  saving.value = true
  try {
    const body = {
      title: draft.title.trim() || null,
      summary: draft.summary.trim() || null
    }
    if (editing.value) {
      await servers.updateImage(props.username, props.servername, editing.value.imageId, {
        ...body,
        ...(draft.resourceId ? { resourceId: draft.resourceId } : {})
      })
    } else {
      await servers.addImage(props.username, props.servername, {
        ...body,
        resourceId: draft.resourceId,
        orderIndex: images.value.length
      })
    }
    editorOpen.value = false
    success(t('manage.saved'))
    await refresh()
  } catch (error) {
    notify(error)
  } finally {
    saving.value = false
  }
}

async function remove(image: GalleryImage) {
  try {
    await servers.removeImage(props.username, props.servername, image.imageId)
    await refresh()
  } catch (error) {
    notify(error)
  }
}

/** Swap an image with its neighbour and persist both order indexes. */
async function move(index: number, delta: -1 | 1) {
  const a = images.value[index]
  const b = images.value[index + delta]
  if (!a || !b) return
  try {
    await Promise.all([
      servers.updateImage(props.username, props.servername, a.imageId, { orderIndex: index + delta }),
      servers.updateImage(props.username, props.servername, b.imageId, { orderIndex: index })
    ])
    await refresh()
  } catch (error) {
    notify(error)
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <p class="text-sm text-muted">
        {{ t('gallery.count', { n: images.length }, images.length) }}
      </p>
      <UButton
        icon="i-lucide-image-plus"
        :label="t('gallery.add')"
        @click="openNew"
      />
    </div>

    <USkeleton
      v-if="status === 'pending'"
      class="h-40"
    />
    <EmptyState
      v-else-if="!images.length"
      icon="i-lucide-images"
      :title="t('gallery.empty')"
    />
    <ul
      v-else
      class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3"
    >
      <li
        v-for="(image, index) in images"
        :key="image.imageId"
        class="overflow-hidden rounded-lg bg-elevated/60"
      >
        <div class="aspect-video">
          <MediaImage
            :src="image.imageUrl"
            :alt="image.imageTitle ?? ''"
          />
        </div>
        <div class="flex items-center gap-1 p-2">
          <p class="min-w-0 flex-1 truncate text-sm">
            {{ image.imageTitle || t('gallery.untitled') }}
          </p>
          <UButton
            icon="i-lucide-arrow-left"
            :aria-label="t('gallery.moveUp')"
            color="neutral"
            variant="ghost"
            size="xs"
            :disabled="index === 0"
            @click="move(index, -1)"
          />
          <UButton
            icon="i-lucide-arrow-right"
            :aria-label="t('gallery.moveDown')"
            color="neutral"
            variant="ghost"
            size="xs"
            :disabled="index === images.length - 1"
            @click="move(index, 1)"
          />
          <UButton
            icon="i-lucide-pencil"
            :aria-label="t('common.edit')"
            color="neutral"
            variant="ghost"
            size="xs"
            @click="openEdit(image)"
          />
          <UButton
            icon="i-lucide-trash-2"
            :aria-label="t('common.delete')"
            color="error"
            variant="ghost"
            size="xs"
            @click="remove(image)"
          />
        </div>
      </li>
    </ul>

    <UModal
      v-model:open="editorOpen"
      :title="editing ? t('gallery.edit') : t('gallery.add')"
    >
      <template #body>
        <form
          id="gallery-form"
          class="flex flex-col gap-4"
          @submit.prevent="saveImage"
        >
          <ImageUploader
            :label="t('gallery.image')"
            :current-url="editing?.imageUrl"
            aspect="video"
            @uploaded="draft.resourceId = $event"
          />
          <UFormField
            :label="t('gallery.imageTitle')"
            :hint="`${draft.title.length}/${LIMITS.imageTitle}`"
          >
            <UInput
              v-model="draft.title"
              :maxlength="LIMITS.imageTitle"
              variant="soft"
              class="w-full"
            />
          </UFormField>
          <UFormField
            :label="t('gallery.imageSummary')"
            :hint="`${draft.summary.length}/${LIMITS.imageSummary}`"
          >
            <UTextarea
              v-model="draft.summary"
              :maxlength="LIMITS.imageSummary"
              :rows="3"
              variant="soft"
              class="w-full"
            />
          </UFormField>
        </form>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton
            :label="t('common.cancel')"
            color="neutral"
            variant="ghost"
            @click="editorOpen = false"
          />
          <UButton
            type="submit"
            form="gallery-form"
            :label="t('common.save')"
            :loading="saving"
            :disabled="!editing && !draft.resourceId"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>
