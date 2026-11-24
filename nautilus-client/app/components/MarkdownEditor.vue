<script setup lang="ts">
const model = defineModel<string>({ default: '' })

const props = withDefaults(defineProps<{
  maxlength?: number
  rows?: number
  placeholder?: string
}>(), {
  maxlength: LIMITS.bio,
  rows: 10,
  placeholder: undefined
})

const { t, n } = useI18n()
const tab = ref('write')

const tabs = computed(() => [
  { label: t('markdown.write'), value: 'write', icon: 'i-lucide-pencil', slot: 'write' as const },
  { label: t('markdown.preview'), value: 'preview', icon: 'i-lucide-eye', slot: 'preview' as const }
])
</script>

<template>
  <div class="flex flex-col gap-2">
    <UTabs
      v-model="tab"
      :items="tabs"
      variant="link"
      size="sm"
      :ui="{ content: 'pt-2' }"
    >
      <template #write>
        <UTextarea
          v-model="model"
          :rows="props.rows"
          :maxlength="props.maxlength"
          :placeholder="props.placeholder ?? t('markdown.placeholder')"
          autoresize
          variant="soft"
          class="w-full font-mono"
        />
      </template>
      <template #preview>
        <div class="min-h-32 rounded-md bg-elevated/50 p-3">
          <MarkdownRender
            v-if="model"
            :source="model"
          />
          <p
            v-else
            class="text-sm text-dimmed"
          >
            {{ t('markdown.empty') }}
          </p>
        </div>
      </template>
    </UTabs>
    <div class="flex justify-between text-xs text-muted">
      <span>{{ t('markdown.hint') }}</span>
      <span>{{ n(model.length) }} / {{ n(props.maxlength) }}</span>
    </div>
  </div>
</template>
