<script setup lang="ts">
const props = defineProps<{
  label: string
  address: string
}>()

const { t } = useI18n()
const copied = ref(false)

async function copy() {
  await navigator.clipboard.writeText(props.address)
  copied.value = true
  setTimeout(() => (copied.value = false), 1500)
}
</script>

<template>
  <div class="flex items-center gap-3 rounded-md border-2 border-(--block-border) bg-(--ui-bg) px-3 py-2">
    <div class="min-w-0 flex-1">
      <p class="text-xs text-muted">
        {{ label }}
      </p>
      <p class="truncate font-pixel text-pixel-base text-highlighted">
        {{ address }}
      </p>
    </div>
    <UButton
      :icon="copied ? 'i-lucide-check' : 'i-lucide-copy'"
      :label="copied ? t('common.copied') : t('common.copy')"
      size="xs"
      @click="copy"
    />
    <span
      class="sr-only"
      aria-live="polite"
    >{{ copied ? t('common.copied') : '' }}</span>
  </div>
</template>
