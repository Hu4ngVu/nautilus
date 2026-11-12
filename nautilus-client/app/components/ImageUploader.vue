<script setup lang="ts">
/**
 * Picks an image (GIF allowed), uploads it to object storage and emits its resourceId.
 * Shows the current image until a new one is chosen.
 */
const props = withDefaults(defineProps<{
  currentUrl?: string | null
  label: string
  aspect?: 'square' | 'banner' | 'video'
  removable?: boolean
}>(), {
  currentUrl: null,
  aspect: 'square',
  removable: false
})

const emit = defineEmits<{
  uploaded: [resourceId: string]
  removed: []
}>()

const { t } = useI18n()
const { upload, uploading } = useUpload()
const { notify } = useApiError()

const input = ref<HTMLInputElement | null>(null)
const preview = ref<string | null>(null)

const shown = computed(() => preview.value ?? props.currentUrl)
const aspectClass = computed(() => ({
  square: 'aspect-square w-28',
  banner: 'aspect-[4/1] w-full',
  video: 'aspect-video w-full'
})[props.aspect])

async function onChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  try {
    const resourceId = await upload(file)
    if (preview.value) URL.revokeObjectURL(preview.value)
    preview.value = URL.createObjectURL(file)
    emit('uploaded', resourceId)
  } catch (error) {
    notify(error, t('errors.upload.title'))
  } finally {
    if (input.value) input.value.value = ''
  }
}

function remove() {
  if (preview.value) URL.revokeObjectURL(preview.value)
  preview.value = null
  emit('removed')
}

onBeforeUnmount(() => {
  if (preview.value) URL.revokeObjectURL(preview.value)
})
</script>

<template>
  <div class="flex flex-col gap-2">
    <span class="text-sm font-medium text-default">{{ label }}</span>
    <div
      class="relative overflow-hidden rounded-md bg-elevated"
      :class="aspectClass"
    >
      <MediaImage
        :src="shown"
        :alt="label"
      />
      <div
        v-if="uploading"
        class="absolute inset-0 flex items-center justify-center bg-default/70"
        role="status"
      >
        <UIcon
          name="i-lucide-loader-circle"
          class="size-6 animate-spin"
        />
        <span class="sr-only">{{ t('upload.uploading') }}</span>
      </div>
    </div>
    <div class="flex flex-wrap gap-2">
      <UButton
        icon="i-lucide-upload"
        :label="t('upload.choose')"
        color="neutral"
        variant="soft"
        size="sm"
        :loading="uploading"
        @click="input?.click()"
      />
      <UButton
        v-if="removable && shown"
        icon="i-lucide-trash-2"
        :label="t('upload.remove')"
        color="error"
        variant="ghost"
        size="sm"
        :disabled="uploading"
        @click="remove"
      />
    </div>
    <p class="text-xs text-muted">
      {{ t('upload.hint', { size: 10 }) }}
    </p>
    <input
      ref="input"
      type="file"
      class="hidden"
      :accept="ALLOWED_IMAGE_TYPES.join(',')"
      @change="onChange"
    >
  </div>
</template>
