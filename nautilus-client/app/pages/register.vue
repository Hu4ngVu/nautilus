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

// Typed by hand: inferring it from the template is circular (the template reads these computeds)
const form = useTemplateRef<{ state: Partial<RegisterState> }>('authForm')
const typedUsername = computed<string>(() => String(form.value?.state.username ?? ''))
const typedEmail = computed<string>(() => String(form.value?.state.email ?? ''))
const typedPassword = computed<string>(() => String(form.value?.state.password ?? ''))

const usernameStatus = useAvailability('username', typedUsername, v => USERNAME_PATTERN.test(v) && !isReservedUsername(v))
const emailStatus = useAvailability('email', typedEmail, v => EMAIL_PATTERN.test(v))
const strength = computed<PasswordStrength>(() => passwordStrength(typedPassword.value))

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
  } else if (isReservedUsername(state.username)) {
    errors.push({ name: 'username', message: t('validation.usernameReserved') })
  }
  if (!state.email || !EMAIL_PATTERN.test(state.email)) {
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
    await navigateTo(`/${encodeURIComponent(username)}`)
  } catch (error) {
    notify(error)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthShell>
    <UAuthForm
      ref="authForm"
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
      <template
        v-for="[field, status] in [['username', usernameStatus], ['email', emailStatus]] as const"
        :key="field"
        #[`${field}-hint`]
      >
        <AvailabilityHint :status="status" />
      </template>
      <template #password-help>
        <div
          v-if="strength"
          class="flex items-center gap-2"
        >
          <div
            class="flex gap-1"
            aria-hidden="true"
          >
            <span
              v-for="i in 4"
              :key="i"
              class="size-3 border-2 border-(--block-border)"
              :class="i <= strength ? ['bg-error', 'bg-warning', 'bg-nautilus-500', 'bg-nautilus-500'][strength - 1] : ''"
            />
          </div>
          <span class="text-xs text-muted">{{ t('auth.strengthLabel', { level: t(`auth.strength.${strength}`) }) }}</span>
        </div>
      </template>
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
  </AuthShell>
</template>
