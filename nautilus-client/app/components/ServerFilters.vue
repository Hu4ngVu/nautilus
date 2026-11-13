<script setup lang="ts">
import type { ServerPlatform, ServerType, TagCategory } from '~/types/api'
import type { ServerQueryState } from '~/composables/useServerQuery'

// `framed` draws the raised panel with its own header; the mobile slideover brings its own chrome
const props = withDefaults(defineProps<{ state: ServerQueryState, framed?: boolean }>(), { framed: true })
const emit = defineEmits<{ update: [patch: Partial<ServerQueryState>] }>()

const { t } = useI18n()
const COLLAPSED_TAGS = 8

type FilterFields = Pick<ServerQueryState, 'serverPlatform' | 'serverType' | 'requiresPremium'> & { tags: string[] }

function fieldsOf(s: ServerQueryState): FilterFields {
  return { serverPlatform: s.serverPlatform, serverType: s.serverType, requiresPremium: s.requiresPremium, tags: [...(s.tags ?? [])] }
}

function sameFields(a: FilterFields, b: FilterFields) {
  return a.serverPlatform === b.serverPlatform && a.serverType === b.serverType
    && a.requiresPremium === b.requiresPremium
    && a.tags.length === b.tags.length && a.tags.every(tag => b.tags.includes(tag))
}

// Picks only change this draft; the list is refetched once, when the draft is applied
const draft = ref(fieldsOf(props.state))

// Applied filters can also change from outside (chip removal, back button); start over from them.
// Keyed on the filter fields only, so paging or re-sorting keeps an unapplied draft.
watch(() => JSON.stringify(fieldsOf(props.state)), () => {
  draft.value = fieldsOf(props.state)
})

const dirty = computed(() => !sameFields(draft.value, fieldsOf(props.state)))

function apply() {
  emit('update', { ...draft.value })
}

interface Choice<T> {
  value: T
  label: string
  icon: string
  /** Fill for the pressed segment; omitted means the neutral inverted fill */
  color?: BlockColor
}

// Pressing the active segment again clears it, so there is no separate "any" option
const platformChoices = computed<Choice<ServerPlatform>[]>(() => [
  { value: 'JAVA', label: PLATFORM_LABEL.JAVA, icon: 'i-lucide-monitor', color: BLOCK_PALETTE[0] },
  { value: 'BEDROCK', label: PLATFORM_LABEL.BEDROCK, icon: 'i-lucide-gamepad-2', color: BLOCK_PALETTE[1] }
])

const typeChoices = computed<Choice<ServerType>[]>(() => [
  { value: 'VANILLA', label: t('server.type.VANILLA'), icon: 'i-lucide-box' },
  { value: 'MODPACK', label: t('server.type.MODPACK'), icon: 'i-lucide-boxes' }
])

const premiumChoices = computed<Choice<boolean>[]>(() => [
  { value: true, label: t('filters.premiumShort'), icon: 'i-lucide-badge-check' },
  { value: false, label: t('filters.crackedShort'), icon: 'i-lucide-lock-open' }
])

const groups = computed(() => [
  {
    id: 'platform',
    title: t('filters.platform'),
    choices: platformChoices.value,
    current: draft.value.serverPlatform,
    pick: (v?: ServerPlatform) => { draft.value.serverPlatform = v }
  },
  {
    id: 'type',
    title: t('filters.type'),
    choices: typeChoices.value,
    current: draft.value.serverType,
    pick: (v?: ServerType) => { draft.value.serverType = v }
  },
  {
    id: 'premium',
    title: t('filters.account'),
    choices: premiumChoices.value,
    current: draft.value.requiresPremium,
    pick: (v?: boolean) => { draft.value.requiresPremium = v }
  }
] as { id: string, title: string, choices: Choice<unknown>[], current: unknown, pick: (v?: unknown) => void }[])

function blockVars(color?: BlockColor) {
  return color
    ? { '--face': color.face, '--shade': color.shade, '--ink': color.ink }
    : { '--face': 'var(--ui-bg-inverted)', '--shade': 'rgb(0 0 0 / 0.35)', '--ink': 'var(--ui-text-inverted)' }
}

const selectedTags = computed(() => new Set(draft.value.tags))

function toggleTag(tag: string) {
  const tags = draft.value.tags
  draft.value.tags = selectedTags.value.has(tag) ? tags.filter(x => x !== tag) : [...tags, tag]
}

function tagLabel(tag: string) {
  return t(`tags.label.${tag}`, tag)
}

const tagSearch = ref('')
const searching = computed(() => tagSearch.value.trim().length > 0)

const expanded = ref(new Set<TagCategory>())

function toggleExpanded(category: TagCategory) {
  const next = new Set(expanded.value)
  if (next.has(category)) next.delete(category)
  else next.add(category)
  expanded.value = next
}

/** Tags to draw per category: search matches, or the collapsed list plus anything already selected. */
const tagSections = computed(() => {
  const needle = tagSearch.value.trim().toLowerCase()
  return TAG_CATEGORIES.map((category) => {
    const all = TAG_CATALOG[category]
    let shown = all
    if (needle) {
      shown = all.filter(tag => tag.toLowerCase().includes(needle) || tagLabel(tag).toLowerCase().includes(needle))
    } else if (!expanded.value.has(category) && all.length > COLLAPSED_TAGS) {
      shown = all.filter((tag, i) => i < COLLAPSED_TAGS || selectedTags.value.has(tag))
    }
    return {
      category,
      color: TAG_CATEGORY_COLOR[category],
      shown,
      hidden: all.length - shown.length,
      picked: all.filter(tag => selectedTags.value.has(tag)).length
    }
  }).filter(section => !needle || section.shown.length)
})

function countOf(f: FilterFields) {
  return [f.serverPlatform, f.serverType, f.requiresPremium].filter(v => v !== undefined).length + f.tags.length
}

const appliedCount = computed(() => countOf(fieldsOf(props.state)))
const draftCount = computed(() => countOf(draft.value))

/** Clears the draft only; like any other pick it takes effect on apply. */
function clearDraft() {
  draft.value = { serverPlatform: undefined, serverType: undefined, requiresPremium: undefined, tags: [] }
}
</script>

<template>
  <div
    class="flex min-h-0 flex-col"
    :class="framed ? 'block-surface block-raised' : 'flex-1'"
  >
    <header
      v-if="framed"
      class="flex items-center gap-2 border-b-2 border-(--block-border) px-4 py-3"
    >
      <h2 class="font-pixel text-pixel-xl text-highlighted">
        {{ t('browse.filters') }}
      </h2>
      <span
        v-if="appliedCount"
        class="grid min-w-6 place-items-center rounded-sm bg-nautilus-400 px-1 py-0.5 font-pixel text-pixel-sm text-gray-950"
      >
        {{ appliedCount }}
      </span>
    </header>

    <div class="flex min-h-0 flex-1 flex-col divide-y-2 divide-default overflow-y-auto">
      <div
        class="flex flex-col gap-4"
        :class="framed ? 'p-4' : 'p-4 sm:p-6'"
      >
        <div
          v-for="group in groups"
          :key="group.id"
          role="group"
          :aria-labelledby="`filter-${group.id}`"
        >
          <h3
            :id="`filter-${group.id}`"
            class="mb-1.5 font-pixel text-pixel-base text-highlighted"
          >
            {{ group.title }}
          </h3>
          <div class="grid grid-cols-2 divide-x-2 divide-(--block-border) overflow-hidden rounded-md border-2 border-(--block-border) bg-(--block-surface)">
            <button
              v-for="choice in group.choices"
              :key="String(choice.value)"
              type="button"
              :aria-pressed="group.current === choice.value"
              :style="blockVars(choice.color)"
              class="flex min-w-0 items-center justify-center gap-1.5 px-2 pt-2 pb-2.5 text-sm font-medium leading-none transition-[background-color,color,box-shadow] duration-150 focus-visible:-outline-offset-4"
              :class="group.current === choice.value
                ? 'bg-(--face) text-(--ink) shadow-[inset_0_-4px_0_var(--shade)]'
                : 'text-muted hover:bg-elevated hover:text-highlighted'"
              @click="group.pick(group.current === choice.value ? undefined : choice.value)"
            >
              <UIcon
                :name="choice.icon"
                class="size-4 shrink-0"
              />
              <span class="truncate">{{ choice.label }}</span>
            </button>
          </div>
        </div>
      </div>

      <div
        class="flex flex-col gap-4"
        :class="framed ? 'p-4' : 'p-4 sm:p-6'"
      >
        <div class="flex flex-col gap-2">
          <h3 class="font-pixel text-pixel-lg text-highlighted">
            {{ t('filters.tags') }}
          </h3>
          <UInput
            v-model="tagSearch"
            icon="i-lucide-search"
            :placeholder="t('filters.searchTags')"
            :aria-label="t('filters.searchTags')"
            size="sm"
            class="w-full"
          />
        </div>

        <p
          v-if="!tagSections.length"
          class="text-sm text-muted"
        >
          {{ t('filters.noTags') }}
        </p>

        <section
          v-for="section in tagSections"
          :key="section.category"
          :style="blockVars(section.color)"
          role="group"
          :aria-labelledby="`tags-${section.category}`"
        >
          <h4
            :id="`tags-${section.category}`"
            class="mb-2 flex items-center gap-2 text-sm font-semibold text-highlighted"
          >
            <span
              class="size-3.5 shrink-0 rounded-[2px] border-2 border-(--block-border) bg-(--face) shadow-[inset_-2px_-2px_0_var(--shade)]"
              aria-hidden="true"
            />
            {{ t(`tags.category.${section.category}`) }}
            <span
              v-if="section.picked"
              class="rounded-sm bg-(--face) px-1.5 py-0.5 text-xs leading-none text-(--ink)"
            >
              {{ section.picked }}
            </span>
          </h4>
          <div class="flex flex-wrap gap-1.5">
            <button
              v-for="tag in section.shown"
              :key="tag"
              type="button"
              :aria-pressed="selectedTags.has(tag)"
              class="rounded-md border-2 px-2 pt-1 pb-1.5 text-sm leading-none transition-[background-color,border-color,box-shadow,transform] duration-150 active:translate-y-px"
              :class="selectedTags.has(tag)
                ? 'border-(--block-border) bg-(--face) font-medium text-(--ink) shadow-[inset_0_-3px_0_var(--shade)]'
                : 'border-default bg-(--block-surface) text-default hover:border-(--shade)'"
              @click="toggleTag(tag)"
            >
              {{ tagLabel(tag) }}
            </button>
            <button
              v-if="!searching && (section.hidden || expanded.has(section.category))"
              type="button"
              :aria-expanded="expanded.has(section.category)"
              class="rounded-md border-2 border-dashed border-accented px-2 pt-1 pb-1.5 text-sm leading-none text-muted hover:border-(--shade) hover:text-highlighted"
              @click="toggleExpanded(section.category)"
            >
              {{ expanded.has(section.category) ? t('filters.showLess') : t('filters.showMoreCount', { n: section.hidden }) }}
            </button>
          </div>
        </section>
      </div>
    </div>

    <footer
      class="flex flex-col gap-2 border-t-2 border-(--block-border)"
      :class="framed ? 'p-4' : 'p-4 sm:px-6'"
    >
      <p
        v-if="dirty"
        class="flex items-center gap-1.5 text-xs text-muted"
        aria-live="polite"
      >
        <span
          class="size-2 shrink-0 rounded-[1px] bg-amber-400"
          aria-hidden="true"
        />
        {{ t('filters.unapplied') }}
      </p>
      <div class="flex gap-2">
        <UButton
          :label="t('filters.reset')"
          color="neutral"
          variant="soft"
          :disabled="!draftCount"
          @click="clearDraft"
        />
        <UButton
          :label="t('filters.apply')"
          icon="i-lucide-check"
          :disabled="!dirty"
          block
          class="flex-1"
          @click="apply"
        />
      </div>
    </footer>
  </div>
</template>
