<script setup lang="ts">
import type { ServerDetail, UpdateServerRequest } from '~/types/api'

const props = defineProps<{
  username: string
  server: ServerDetail
}>()

const emit = defineEmits<{ saved: [] }>()

const { t } = useI18n()
const servers = useServerApi()
const { notify, success } = useApiError()

const form = reactive({
  summary: props.server.summary ?? '',
  bio: props.server.bio ?? '',
  javaAddress: props.server.javaAddress ?? '',
  bedrockAddress: props.server.bedrockAddress ?? '',
  requiresPremium: props.server.requiresPremium
})

// undefined = unchanged, null = removed, string = new resource
const avatarResourceId = ref<string | null | undefined>(undefined)
const bannerResourceId = ref<string | null | undefined>(undefined)
const saving = ref(false)

const addressError = (value: string) =>
  value && (!ADDRESS_PATTERN.test(value) || value.length > LIMITS.address) ? t('validation.address') : undefined

const javaError = computed(() => addressError(form.javaAddress))
const bedrockError = computed(() => addressError(form.bedrockAddress))
const summaryError = computed(() => !form.summary.trim() ? t('validation.required') : undefined)
const invalid = computed(() => !!javaError.value || !!bedrockError.value || !!summaryError.value)

const dirty = computed(() => {
  const s = props.server
  return form.summary !== (s.summary ?? '')
    || form.bio !== (s.bio ?? '')
    || form.javaAddress !== (s.javaAddress ?? '')
    || form.bedrockAddress !== (s.bedrockAddress ?? '')
    || form.requiresPremium !== s.requiresPremium
    || avatarResourceId.value !== undefined
    || bannerResourceId.value !== undefined
})

function discard() {
  const s = props.server
  Object.assign(form, {
    summary: s.summary ?? '',
    bio: s.bio ?? '',
    javaAddress: s.javaAddress ?? '',
    bedrockAddress: s.bedrockAddress ?? '',
    requiresPremium: s.requiresPremium
  })
  avatarResourceId.value = undefined
  bannerResourceId.value = undefined
}

function orNull(value: string) {
  return value.trim() ? value.trim() : null
}

async function save() {
  if (invalid.value) return
  const s = props.server
  const body: UpdateServerRequest = {}
  if (form.summary !== s.summary) body.summary = form.summary.trim()
  if (form.bio !== (s.bio ?? '')) body.bio = orNull(form.bio)
  if (form.javaAddress !== (s.javaAddress ?? '')) body.javaAddress = orNull(form.javaAddress)
  if (form.bedrockAddress !== (s.bedrockAddress ?? '')) body.bedrockAddress = orNull(form.bedrockAddress)
  if (form.requiresPremium !== s.requiresPremium) body.requiresPremium = form.requiresPremium
  if (avatarResourceId.value !== undefined) body.avatarResourceId = avatarResourceId.value
  if (bannerResourceId.value !== undefined) body.bannerResourceId = bannerResourceId.value
  if (!Object.keys(body).length) return

  saving.value = true
  try {
    await servers.update(props.username, s.servername, body)
    avatarResourceId.value = undefined
    bannerResourceId.value = undefined
    success(t('manage.saved'))
    emit('saved')
  } catch (error) {
    notify(error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <form
    class="flex flex-col gap-6"
    @submit.prevent="save"
  >
    <ImageUploader
      :label="t('manage.banner')"
      :current-url="bannerResourceId === null ? null : server.bannerUrl"
      aspect="banner"
      removable
      @uploaded="bannerResourceId = $event"
      @removed="bannerResourceId = null"
    />
    <ImageUploader
      :label="t('manage.avatar')"
      :current-url="avatarResourceId === null ? null : server.avatarUrl"
      removable
      @uploaded="avatarResourceId = $event"
      @removed="avatarResourceId = null"
    />

    <UFormField
      :label="t('create.summary')"
      :hint="`${form.summary.length}/${LIMITS.summary}`"
      :error="summaryError"
      required
    >
      <UTextarea
        v-model="form.summary"
        :maxlength="LIMITS.summary"
        :rows="3"
        class="w-full"
      />
    </UFormField>

    <div class="grid gap-4 sm:grid-cols-2">
      <UFormField
        :label="t('server.javaAddress')"
        :error="javaError"
        :help="t('manage.addressHelp')"
      >
        <UInput
          v-model="form.javaAddress"
          placeholder="play.example.com:25565"
          class="w-full"
        />
      </UFormField>
      <UFormField
        :label="t('server.bedrockAddress')"
        :error="bedrockError"
        :help="t('manage.addressHelp')"
      >
        <UInput
          v-model="form.bedrockAddress"
          placeholder="play.example.com:19132"
          class="w-full"
        />
      </UFormField>
    </div>

    <USwitch
      v-model="form.requiresPremium"
      :label="t('create.premium')"
      :description="t('create.premiumHelp')"
    />

    <UFormField :label="t('server.about')">
      <LazyMarkdownEditor v-model="form.bio" />
    </UFormField>

    <!-- Sticks to the bottom of the viewport while there are unsaved changes -->
    <div
      class="sticky bottom-4 z-10 flex flex-wrap items-center justify-between gap-3 p-3 block-surface"
      :class="dirty ? 'block-raised lift-primary border-nautilus-500!' : ''"
    >
      <p
        class="text-sm"
        :class="dirty ? 'text-highlighted' : 'text-muted'"
        aria-live="polite"
      >
        {{ dirty ? t('manage.unsaved') : t('manage.noChanges') }}
      </p>
      <div class="flex gap-2">
        <UButton
          :label="t('manage.discard')"
          color="neutral"
          variant="ghost"
          :disabled="!dirty || saving"
          @click="discard"
        />
        <UButton
          type="submit"
          :label="t('common.save')"
          :loading="saving"
          :disabled="invalid || !dirty"
        />
      </div>
    </div>
  </form>
</template>
