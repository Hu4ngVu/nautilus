<script setup lang="ts">
const { locale, locales, setLocale, t } = useI18n()

const items = computed(() => locales.value.map(l => ({ label: l.name ?? l.code, value: l.code })))
</script>

<template>
  <USelect
    :model-value="locale"
    :items="items"
    icon="i-lucide-languages"
    variant="soft"
    size="sm"
    :aria-label="t('settings.language')"
    class="w-36"
    @update:model-value="setLocale($event as typeof locale)"
  />
</template>
