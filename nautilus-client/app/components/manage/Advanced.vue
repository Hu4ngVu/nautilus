<script setup lang="ts">
import type { ServerDetail } from '~/types/api'

const props = defineProps<{
  username: string
  server: ServerDetail
}>()

const emit = defineEmits<{ saved: [], renamed: [servername: string] }>()

const { t } = useI18n()
const servers = useServerApi()
const { notify, success } = useApiError()

const visibility = ref(props.server.visibility)
const togglingVisibility = ref(false)

async function setVisibility(value: boolean) {
  togglingVisibility.value = true
  try {
    await servers.setVisibility(props.username, props.server.servername, value)
    visibility.value = value
    success(t('manage.saved'))
    emit('saved')
  } catch (error) {
    notify(error)
  } finally {
    togglingVisibility.value = false
  }
}

const newName = ref('')
const renaming = ref(false)
const nameError = computed(() =>
  newName.value && !SERVERNAME_PATTERN.test(newName.value) ? t('validation.servername', { max: LIMITS.servername }) : undefined
)

async function rename() {
  if (!newName.value || nameError.value) return
  renaming.value = true
  try {
    await servers.rename(props.username, props.server.servername, newName.value)
    success(t('manage.renamed'))
    emit('renamed', newName.value)
  } catch (error) {
    notify(error)
  } finally {
    renaming.value = false
  }
}

const deleteOpen = ref(false)
const confirmName = ref('')
const deleting = ref(false)

async function remove() {
  deleting.value = true
  try {
    await servers.remove(props.username, props.server.servername)
    success(t('manage.deleted'))
    await navigateTo('/dashboard')
  } catch (error) {
    notify(error)
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-8">
    <USwitch
      :model-value="visibility"
      :loading="togglingVisibility"
      :label="t('create.visibility')"
      :description="t('create.visibilityHelp')"
      @update:model-value="setVisibility"
    />

    <form
      class="flex flex-col gap-3 sm:flex-row sm:items-start"
      @submit.prevent="rename"
    >
      <UFormField
        :label="t('manage.rename')"
        :error="nameError"
        class="flex-1"
      >
        <UInput
          v-model="newName"
          :placeholder="server.servername"
          :maxlength="LIMITS.servername"
          class="w-full"
        />
      </UFormField>
      <UButton
        type="submit"
        :label="t('manage.renameSubmit')"
        :loading="renaming"
        :disabled="!newName || !!nameError"
        color="neutral"
        variant="soft"
        class="sm:mt-6"
      />
    </form>

    <div class="rounded-lg border-2 border-red-500 p-4 shadow-[4px_4px_0_var(--color-red-500)]">
      <h3 class="font-pixel text-pixel-xl text-error">
        {{ t('manage.dangerZone') }}
      </h3>
      <p class="mt-1 text-sm text-muted">
        {{ t('manage.deleteHelp') }}
      </p>
      <UButton
        icon="i-lucide-trash-2"
        :label="t('manage.delete')"
        color="error"
        class="mt-3"
        @click="deleteOpen = true"
      />
    </div>

    <UModal
      v-model:open="deleteOpen"
      :title="t('manage.deleteTitle')"
      :description="t('manage.deleteConfirm', { name: server.servername })"
    >
      <template #body>
        <UInput
          v-model="confirmName"
          :placeholder="server.servername"
          :aria-label="t('manage.deleteTitle')"
          class="w-full"
        />
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton
            :label="t('common.cancel')"
            color="neutral"
            variant="ghost"
            @click="deleteOpen = false"
          />
          <UButton
            :label="t('manage.delete')"
            color="error"
            :loading="deleting"
            :disabled="confirmName !== server.servername"
            @click="remove"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>
