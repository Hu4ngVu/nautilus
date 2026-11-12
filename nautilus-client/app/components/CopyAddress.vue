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
  <div class="flex items-center gap-3 rounded-md bg-elevated/60 px-3 py-2">
    <div class="min-w-0 flex-1">
      <p class="text-xs text-muted">
        {{ label }}
      </p>
      <p class="truncate font-mono text-sm text-highlighted">
        {{ address }}
      </p>
    </div>
    <UButton
      :icon="copied ? 'i-lucide-check' : 'i-lucide-copy'"
      :aria-label="t('common.copy')"
      color="neutral"
      variant="ghost"
      size="sm"
      @click="copy"
    />
    <span
      class="sr-only"
      aria-live="polite"
    >{{ copied ? t('common.copied') : '' }}</span>
  </div>
</template>
