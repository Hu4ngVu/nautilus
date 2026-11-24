<script setup lang="ts">
import type { TagCategory, TagMap } from '~/types/api'

const props = defineProps<{
  username: string
  servername: string
}>()

const { t } = useI18n()
const servers = useServerApi()
const { notify, success } = useApiError()

const selected = reactive<Record<TagCategory, string[]>>({ FEATURES: [], GAMEPLAY: [], META: [], COMMUNITY: [] })
const saving = ref(false)

const { status } = useAsyncData(`tags:${props.username}/${props.servername}`, async () => {
  const res = await servers.tags(props.username, props.servername)
  for (const category of TAG_CATEGORIES) selected[category] = [...(res.tags[category] ?? [])]
  return res
})

async function save() {
  const tags: TagMap = {}
  for (const category of TAG_CATEGORIES) tags[category] = selected[category]
  saving.value = true
  try {
    await servers.setTags(props.username, props.servername, tags)
    success(t('manage.saved'))
  } catch (error) {
    notify(error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <USkeleton
      v-if="status === 'pending'"
      class="h-40"
    />
    <template v-else>
      <UFormField
        v-for="category in TAG_CATEGORIES"
        :key="category"
        :label="t(`tags.category.${category}`)"
        :hint="`${selected[category].length}/${MAX_TAGS_PER_CATEGORY}`"
      >
        <div class="flex flex-wrap gap-2">
          <UButton
            v-for="tag in TAG_CATALOG[category]"
            :key="tag"
            :label="tag"
            size="sm"
            :color="selected[category].includes(tag) ? 'primary' : 'neutral'"
            :variant="selected[category].includes(tag) ? 'solid' : 'soft'"
            :aria-pressed="selected[category].includes(tag)"
            :disabled="!selected[category].includes(tag) && selected[category].length >= MAX_TAGS_PER_CATEGORY"
            @click="selected[category] = selected[category].includes(tag)
              ? selected[category].filter(x => x !== tag)
              : [...selected[category], tag]"
          />
        </div>
      </UFormField>
      <UButton
        :label="t('common.save')"
        :loading="saving"
        class="self-start"
        @click="save"
      />
    </template>
  </div>
</template>
