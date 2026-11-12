<script setup lang="ts">
import type { ServerPlatform, ServerType, TagCategory } from '~/types/api'
import type { ServerQueryState } from '~/composables/useServerQuery'

const props = defineProps<{ state: ServerQueryState }>()
const emit = defineEmits<{ update: [patch: Partial<ServerQueryState>] }>()

const { t } = useI18n()
const ANY = 'any'
const COLLAPSED_TAGS = 6

const platformItems = computed(() => [
  { label: t('filters.any'), value: ANY },
  { label: 'Java', value: 'JAVA' },
  { label: 'Bedrock', value: 'BEDROCK' }
])

const typeItems = computed(() => [
  { label: t('filters.any'), value: ANY },
  { label: t('server.type.VANILLA'), value: 'VANILLA' },
  { label: t('server.type.MODPACK'), value: 'MODPACK' }
])

const premiumItems = computed(() => [
  { label: t('filters.any'), value: ANY },
  { label: t('filters.premiumOnly'), value: 'true' },
  { label: t('filters.crackedAllowed'), value: 'false' }
])

const platform = computed({
  get: () => props.state.serverPlatform ?? ANY,
  set: v => emit('update', { serverPlatform: v === ANY ? undefined : v as ServerPlatform })
})

const type = computed({
  get: () => props.state.serverType ?? ANY,
  set: v => emit('update', { serverType: v === ANY ? undefined : v as ServerType })
})

const premium = computed({
  get: () => props.state.requiresPremium === undefined ? ANY : String(props.state.requiresPremium),
  set: v => emit('update', { requiresPremium: v === ANY ? undefined : v === 'true' })
})

// One checkbox group per category, all writing into the same flat tag list
const selectedTags = computed(() => new Set(props.state.tags ?? []))

function tagsIn(category: TagCategory) {
  return TAG_CATALOG[category].filter(tag => selectedTags.value.has(tag))
}

function setTagsIn(category: TagCategory, picked: string[]) {
  const others = (props.state.tags ?? []).filter(tag => !TAG_CATALOG[category].includes(tag))
  emit('update', { tags: [...others, ...picked] })
}

const expanded = ref(new Set<TagCategory>())

function toggleExpanded(category: TagCategory) {
  const next = new Set(expanded.value)
  if (next.has(category)) next.delete(category)
  else next.add(category)
  expanded.value = next
}

function visibleTags(category: TagCategory) {
  const all = TAG_CATALOG[category]
  if (expanded.value.has(category) || all.length <= COLLAPSED_TAGS) return all
  // Keep selected tags visible even when the list is collapsed
  return all.filter((tag, i) => i < COLLAPSED_TAGS || selectedTags.value.has(tag))
}

const hasFilters = computed(() =>
  !!props.state.serverPlatform || !!props.state.serverType
  || props.state.requiresPremium !== undefined || !!props.state.tags?.length
)

function reset() {
  emit('update', { serverPlatform: undefined, serverType: undefined, requiresPremium: undefined, tags: [] })
}
</script>

<template>
  <div class="flex flex-col gap-3">
    <UButton
      v-if="hasFilters"
      :label="t('filters.reset')"
      icon="i-lucide-x"
      color="neutral"
      variant="soft"
      block
      @click="reset"
    />

    <section class="rounded-lg bg-elevated/60 p-4">
      <h3 class="mb-3 font-semibold text-highlighted">
        {{ t('filters.platform') }}
      </h3>
      <URadioGroup
        v-model="platform"
        :items="platformItems"
      />
    </section>

    <section class="rounded-lg bg-elevated/60 p-4">
      <h3 class="mb-3 font-semibold text-highlighted">
        {{ t('filters.type') }}
      </h3>
      <URadioGroup
        v-model="type"
        :items="typeItems"
      />
    </section>

    <section class="rounded-lg bg-elevated/60 p-4">
      <h3 class="mb-3 font-semibold text-highlighted">
        {{ t('filters.account') }}
      </h3>
      <URadioGroup
        v-model="premium"
        :items="premiumItems"
      />
    </section>

    <section
      v-for="category in TAG_CATEGORIES"
      :key="category"
      class="rounded-lg bg-elevated/60 p-4"
    >
      <h3 class="mb-3 font-semibold text-highlighted">
        {{ t(`tags.category.${category}`) }}
      </h3>
      <UCheckboxGroup
        :model-value="tagsIn(category)"
        :items="visibleTags(category)"
        @update:model-value="setTagsIn(category, $event as string[])"
      />
      <UButton
        v-if="TAG_CATALOG[category].length > COLLAPSED_TAGS"
        :label="expanded.has(category) ? t('filters.showLess') : t('filters.showMore')"
        :trailing-icon="expanded.has(category) ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'"
        color="neutral"
        variant="link"
        size="sm"
        class="mt-2 -ms-2.5"
        @click="toggleExpanded(category)"
      />
    </section>
  </div>
</template>
