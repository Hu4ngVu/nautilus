<script setup lang="ts">
import type { AuthFormField, FormError, FormSubmitEvent } from '@nuxt/ui'

definePageMeta({ middleware: 'guest' })

interface RegisterState {
  username: string
  email: string
  password: string
  confirmPassword: string
}

const { t } = useI18n()
const auth = useAuthStore()
const users = useUserApi()
const { notify, success } = useApiError()

useSeoMeta({ title: () => t('auth.register.title') })

const loading = ref(false)
const errorMessage = ref<string | null>(null)

const fields = computed<AuthFormField[]>(() => [
  { name: 'username', type: 'text', label: t('auth.username'), placeholder: t('auth.usernamePlaceholder'), help: t('auth.usernameHelp'), required: true, autocomplete: 'username' },
  { name: 'email', type: 'email', label: t('auth.email'), placeholder: 'you@example.com', required: true, autocomplete: 'email' },
  { name: 'password', type: 'password', label: t('auth.password'), placeholder: t('auth.passwordPlaceholder'), required: true, autocomplete: 'new-password' },
  { name: 'confirmPassword', type: 'password', label: t('auth.confirmPassword'), required: true, autocomplete: 'new-password' }
])

const providers = computed(() => [{
  label: t('auth.googleRegister'),
  icon: 'i-simple-icons-google',
  color: 'neutral' as const,
  variant: 'soft' as const,
  onClick: () => auth.loginWithGoogle()
}])

function validate(state: Partial<RegisterState>): FormError[] {
  const errors: FormError[] = []
  if (!state.username || !USERNAME_PATTERN.test(state.username)) {
    errors.push({ name: 'username', message: t('validation.username') })
  }
  if (!state.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(state.email)) {
    errors.push({ name: 'email', message: t('validation.email') })
  }
  if (!state.password || state.password.length < 8 || state.password.length > LIMITS.password) {
    errors.push({ name: 'password', message: t('validation.password', { min: 8, max: LIMITS.password }) })
  }
  if (state.confirmPassword !== state.password) {
    errors.push({ name: 'confirmPassword', message: t('validation.passwordMismatch') })
  }
  return errors
}

async function onSubmit(event: FormSubmitEvent<RegisterState>) {
  const { username, email, password } = event.data
  loading.value = true
  errorMessage.value = null
  try {
    const [usernameTaken, emailTaken] = await Promise.all([users.usernameTaken(username), users.emailTaken(email)])
    if (usernameTaken || emailTaken) {
      errorMessage.value = usernameTaken ? t('auth.register.usernameTaken') : t('auth.register.emailTaken')
      return
    }
    await auth.register({ username, email, password })
    await auth.login({ username, password })
    success(t('auth.register.success'))
    await navigateTo(`/u/${encodeURIComponent(username)}`)
  } catch (error) {
    notify(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <UContainer class="flex justify-center py-12">
    <div class="w-full max-w-sm rounded-lg bg-elevated/60 p-6 sm:p-8">
      <UAuthForm
        :title="t('auth.register.title')"
        :description="t('auth.register.description')"
        icon="i-lucide-user-plus"
        :fields="fields"
        :providers="providers"
        :separator="t('auth.or')"
        :submit="{ label: t('auth.register.submit'), block: true }"
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
            keypath="auth.register.hasAccount"
            tag="p"
            class="text-center text-sm text-muted"
          >
            <template #link>
              <ULink
                to="/login"
                class="font-medium text-secondary"
              >
                {{ t('auth.login.link') }}
              </ULink>
            </template>
          </i18n-t>
        </template>
      </UAuthForm>
    </div>
  </UContainer>
</template>
