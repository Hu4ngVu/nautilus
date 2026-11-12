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

const platformItems = [
  { label: 'Java', value: 'JAVA' },
  { label: 'Bedrock', value: 'BEDROCK' }
]

const typeItems = computed(() => [
  { label: t('server.type.VANILLA'), value: 'VANILLA' },
  { label: t('server.type.MODPACK'), value: 'MODPACK' }
])

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
    await navigateTo(`/s/${encodeURIComponent(auth.username)}/${encodeURIComponent(event.data.servername)}/manage`)
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
    <h1 class="text-2xl font-bold text-highlighted">
      {{ t('create.title') }}
    </h1>
    <p class="mt-1 mb-8 text-muted">
      {{ t('create.subtitle') }}
    </p>

    <UForm
      :state="state"
      :validate="validate"
      class="flex flex-col gap-5 rounded-lg bg-elevated/50 p-5"
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
          variant="soft"
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
          variant="soft"
          class="w-full"
        />
      </UFormField>

      <div class="grid gap-5 sm:grid-cols-2">
        <UFormField
          name="platform"
          :label="t('create.platform')"
        >
          <URadioGroup
            v-model="state.platform"
            :items="platformItems"
            orientation="horizontal"
          />
        </UFormField>
        <UFormField
          name="type"
          :label="t('create.type')"
        >
          <URadioGroup
            v-model="state.type"
            :items="typeItems"
            orientation="horizontal"
          />
        </UFormField>
      </div>

      <USwitch
        v-model="state.visibility"
        :label="t('create.visibility')"
        :description="t('create.visibilityHelp')"
      />
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
