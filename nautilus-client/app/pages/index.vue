<script setup lang="ts">
const { t } = useI18n()

useSeoMeta({ title: () => t('home.tagline') })

const search = ref('')

function submitSearch() {
  const query = search.value.trim()
  navigateTo({ path: '/browse', query: query ? { query } : undefined })
}
</script>

<template>
  <UContainer class="flex min-h-[calc(100svh-var(--ui-header-height)-8rem)] flex-col items-center justify-center gap-8 py-16 text-center">
    <div class="flex flex-col items-center gap-4">
      <UIcon
        name="i-lucide-shell"
        class="size-20 text-primary sm:size-24"
      />
      <span class="text-4xl font-bold tracking-tight text-highlighted sm:text-5xl">Nautilus</span>
    </div>

    <h1 class="max-w-2xl text-xl text-toned text-balance sm:text-2xl">
      {{ t('home.tagline') }}
    </h1>

    <form
      role="search"
      class="w-full max-w-md"
      @submit.prevent="submitSearch"
    >
      <UInput
        v-model="search"
        icon="i-lucide-search"
        size="xl"
        :placeholder="t('home.searchPlaceholder')"
        :aria-label="t('browse.search')"
        variant="soft"
        class="w-full"
      />
    </form>

    <div class="flex flex-wrap justify-center gap-3">
      <UButton
        to="/browse"
        icon="i-lucide-compass"
        :label="t('home.browse')"
        size="lg"
      />
      <UButton
        to="/dashboard"
        icon="i-lucide-layout-dashboard"
        :label="t('home.dashboard')"
        size="lg"
        color="neutral"
        variant="soft"
      />
    </div>
  </UContainer>
</template>
