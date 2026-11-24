<script setup lang="ts">
import type { SelectMenuItem } from '@nuxt/ui'
import type { VersionMap } from '~/types/api'

const props = defineProps<{ label: string }>()
const versions = defineModel<VersionMap>('versions', { required: true })
const primary = defineModel<string | null>('primary', { required: true })

const { t } = useI18n()

const items = computed<SelectMenuItem[]>(() => Object.entries(MC_VERSION_GROUPS).flatMap(([group, list]) => [
  { type: 'label' as const, label: group },
  ...list.map(v => ({ label: v, value: v }))
]))

const selected = computed({
  get: () => Object.values(versions.value).flat(),
  set: (list: string[]) => {
    const next: VersionMap = {}
    for (const v of list) {
      const group = versionGroupOf(v)
      if (group) (next[group] ??= []).push(v)
    }
    versions.value = next
    if (primary.value && !list.includes(primary.value)) primary.value = list[0] ?? null
    if (!primary.value && list.length) primary.value = list[0]!
  }
})

const primaryModel = computed({
  get: () => primary.value ?? undefined,
  set: (v?: string) => {
    primary.value = v ?? null
  }
})

const primaryItems = computed(() => selected.value.map(v => ({ label: v, value: v })))
</script>

<template>
  <div class="grid gap-4 sm:grid-cols-[1fr_12rem]">
    <UFormField :label="props.label">
      <USelectMenu
        v-model="selected"
        :items="items"
        value-key="value"
        multiple
        :placeholder="t('versions.choose')"
        :search-input="{ placeholder: t('versions.search') }"
        variant="soft"
        class="w-full"
      />
    </UFormField>
    <UFormField
      :label="t('versions.primary')"
      :help="t('versions.primaryHelp')"
    >
      <USelect
        v-model="primaryModel"
        :items="primaryItems"
        :disabled="!selected.length"
        variant="soft"
        class="w-full"
      />
    </UFormField>
  </div>
</template>
