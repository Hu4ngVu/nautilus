<script setup lang="ts">
import type { FormError, FormSubmitEvent } from '@nuxt/ui'
import type { CreateServerRequest } from '~/types/api'

definePageMeta({ middleware: 'auth' })

const { t } = useI18n()
const auth = useAuthStore()
const servers = useServerApi()
const { notify, success, fail } = useApiError()

useSeoMeta({ title: () => t('create.title') })

const state = reactive<CreateServerRequest>({
  servername: '',
  summary: '',
  platform: 'JAVA',
  type: 'VANILLA',
  visibility: true,
  requiresPremium: false
})

const loading = ref(false)

const platformItems = computed(() => [
  { label: 'Java', description: t('create.platformHint.JAVA'), value: 'JAVA', icon: 'i-lucide-coffee' },
  { label: 'Bedrock', description: t('create.platformHint.BEDROCK'), value: 'BEDROCK', icon: 'i-lucide-smartphone' }
])

const typeItems = computed(() => [
  { label: t('server.type.VANILLA'), description: t('create.typeHint.VANILLA'), value: 'VANILLA', icon: 'i-lucide-box' },
  { label: t('server.type.MODPACK'), description: t('create.typeHint.MODPACK'), value: 'MODPACK', icon: 'i-lucide-package' }
])

const visibilityItems = computed(() => [
  { label: t('server.public'), description: t('create.visibilityHint.public'), value: 'public', icon: 'i-lucide-globe' },
  { label: t('server.private'), description: t('create.visibilityHint.private'), value: 'private', icon: 'i-lucide-lock' }
])

// Radio values are strings; the request wants a boolean
const visibilityChoice = computed({
  get: () => state.visibility ? 'public' : 'private',
  set: (v: string) => {
    state.visibility = v === 'public'
  }
})

const CARD_RADIO_UI = {
  fieldset: 'grid gap-3 sm:grid-cols-2',
  item: 'items-start gap-3 rounded-lg border-2 border-(--block-border) ring-0 bg-(--block-surface) has-data-[state=checked]:border-nautilus-500 has-data-[state=checked]:shadow-[4px_4px_0_var(--color-nautilus-500)]',
  label: 'font-pixel text-pixel-lg'
}

function validate(s: Partial<CreateServerRequest>): FormError[] {
  const errors: FormError[] = []
  if (!s.servername || !SERVERNAME_PATTERN.test(s.servername)) {
    errors.push({ name: 'servername', message: t('validation.servername', { max: LIMITS.servername }) })
  }
  if (!s.summary?.trim()) {
    errors.push({ name: 'summary', message: t('validation.required') })
  } else if (s.summary.length > LIMITS.summary) {
    errors.push({ name: 'summary', message: t('validation.maxLength', { max: LIMITS.summary }) })
  }
  return errors
}

async function onSubmit(event: FormSubmitEvent<CreateServerRequest>) {
  if (!auth.username) return
  loading.value = true
  try {
    await servers.create(auth.username, event.data)
    success(t('create.success'))
    await navigateTo(`/${encodeURIComponent(auth.username)}/${encodeURIComponent(event.data.servername)}/manage`)
  } catch (error) {
    if (statusOf(error) === 400) {
      fail(t('create.failed'))
    } else {
      notify(error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <UContainer class="max-w-2xl py-8">
    <PageHeader
      :title="t('create.title')"
      :description="t('create.subtitle')"
    />

    <UForm
      :state="state"
      :validate="validate"
      class="flex flex-col gap-6 p-5 block-surface sm:p-6"
      @submit="onSubmit"
    >
      <UFormField
        name="servername"
        :label="t('create.servername')"
        :help="t('create.servernameHelp')"
        required
      >
        <UInput
          v-model="state.servername"
          :maxlength="LIMITS.servername"
          class="w-full"
        />
      </UFormField>

      <UFormField
        name="summary"
        :label="t('create.summary')"
        :hint="`${state.summary.length}/${LIMITS.summary}`"
        required
      >
        <UTextarea
          v-model="state.summary"
          :maxlength="LIMITS.summary"
          :rows="3"
          class="w-full"
        />
      </UFormField>

      <UFormField
        name="platform"
        :label="t('create.platform')"
      >
        <URadioGroup
          v-model="state.platform"
          :items="platformItems"
          variant="card"
          orientation="horizontal"
          indicator="hidden"
          :ui="CARD_RADIO_UI"
        />
      </UFormField>

      <UFormField
        name="type"
        :label="t('create.type')"
      >
        <URadioGroup
          v-model="state.type"
          :items="typeItems"
          variant="card"
          orientation="horizontal"
          indicator="hidden"
          :ui="CARD_RADIO_UI"
        />
      </UFormField>

      <UFormField
        name="visibility"
        :label="t('create.visibilityLabel')"
      >
        <URadioGroup
          v-model="visibilityChoice"
          :items="visibilityItems"
          variant="card"
          orientation="horizontal"
          indicator="hidden"
          :ui="CARD_RADIO_UI"
        />
      </UFormField>

      <USwitch
        v-model="state.requiresPremium"
        :label="t('create.premium')"
        :description="t('create.premiumHelp')"
      />

      <div class="flex justify-end gap-2">
        <UButton
          :label="t('common.cancel')"
          color="neutral"
          variant="ghost"
          @click="$router.back()"
        />
        <UButton
          type="submit"
          :label="t('create.submit')"
          :loading="loading"
        />
      </div>
    </UForm>
  </UContainer>
</template>
