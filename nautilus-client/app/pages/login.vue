<script setup lang="ts">
import type { AuthFormField, FormError, FormSubmitEvent } from '@nuxt/ui'

definePageMeta({ middleware: 'guest' })

const { t } = useI18n()
const auth = useAuthStore()
const route = useRoute()
const { notify } = useApiError()

useSeoMeta({ title: () => t('auth.login.title') })

const loading = ref(false)
const errorMessage = ref<string | null>(null)

const fields = computed<AuthFormField[]>(() => [
  { name: 'username', type: 'text', label: t('auth.identifier'), placeholder: t('auth.identifierPlaceholder'), required: true, autocomplete: 'username' },
  { name: 'password', type: 'password', label: t('auth.password'), placeholder: t('auth.passwordPlaceholder'), required: true, autocomplete: 'current-password' }
])

function validate(state: Partial<{ username: string, password: string }>): FormError[] {
  const errors: FormError[] = []
  if (!state.username) errors.push({ name: 'username', message: t('validation.required') })
  if (!state.password) errors.push({ name: 'password', message: t('validation.required') })
  return errors
}

async function onSubmit(event: FormSubmitEvent<{ username: string, password: string }>) {
  loading.value = true
  errorMessage.value = null
  try {
    await auth.login(event.data)
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/') ? route.query.redirect : '/'
    await navigateTo(redirect)
  } catch (error) {
    const status = statusOf(error)
    if (status === 400 || status === 401) {
      errorMessage.value = t('auth.login.invalid')
    } else {
      notify(error)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell>
    <UAuthForm
      :title="t('auth.login.title')"
      :description="t('auth.login.description')"

      :fields="fields"
      :submit="{ label: t('auth.login.submit'), block: true }"
      :validate="validate"
      :loading="loading"
      @submit="onSubmit"
    >
      <template #validation>
        <UAlert
          v-if="errorMessage"
          color="error"
          variant="soft"
          icon="i-lucide-circle-alert"
          :title="errorMessage"
        />
      </template>
      <template #footer>
        <i18n-t
          keypath="auth.login.noAccount"
          tag="p"
          class="text-center text-sm text-muted"
        >
          <template #link>
            <ULink
              to="/register"
              class="font-medium text-secondary"
            >
              {{ t('auth.register.link') }}
            </ULink>
          </template>
        </i18n-t>
      </template>
    </UAuthForm>
  </AuthShell>
</template>
