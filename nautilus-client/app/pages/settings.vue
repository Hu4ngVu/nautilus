<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const { t } = useI18n()
const auth = useAuthStore()
const stars = useStarsStore()
const users = useUserApi()
const colorMode = useColorMode()
const { notify, success, fail } = useApiError()

useSeoMeta({ title: () => t('settings.title') })

const { data: me, refresh } = useAsyncData('me', () => auth.username ? users.get(auth.username) : Promise.resolve(null), {
  watch: [() => auth.username]
})

const themeItems = computed(() => [
  { label: t('settings.themeSystem'), value: 'system', icon: 'i-lucide-monitor' },
  { label: t('settings.themeLight'), value: 'light', icon: 'i-lucide-sun' },
  { label: t('settings.themeDark'), value: 'dark', icon: 'i-lucide-moon' }
])

// Profile
const bio = ref('')
const savingBio = ref(false)

watch(me, (value) => {
  bio.value = value?.bio ?? ''
}, { immediate: true })

async function saveAvatar(resourceId: string | null) {
  if (!auth.username) return
  try {
    await users.update(auth.username, { resourceId })
    await refresh()
    success(t('settings.avatarSaved'))
  } catch (error) {
    notify(error)
  }
}

async function saveBio() {
  if (!auth.username) return
  savingBio.value = true
  try {
    await users.update(auth.username, { bio: bio.value.trim() ? bio.value : null })
    await refresh()
    success(t('settings.bioSaved'))
  } catch (error) {
    notify(error)
  } finally {
    savingBio.value = false
  }
}

// Username
const newUsername = ref('')
const renaming = ref(false)
const usernameError = computed(() =>
  newUsername.value && !USERNAME_PATTERN.test(newUsername.value) ? t('validation.username') : undefined
)

async function rename() {
  if (!auth.username || !newUsername.value || usernameError.value) return
  renaming.value = true
  try {
    if (await users.usernameTaken(newUsername.value)) {
      fail(t('auth.register.usernameTaken'))
      return
    }
    await users.rename(auth.username, newUsername.value)
    // The access token still carries the old username
    await auth.refresh()
    stars.reset()
    newUsername.value = ''
    success(t('settings.usernameSaved'))
  } catch (error) {
    notify(error)
  } finally {
    renaming.value = false
  }
}

// Password
const passwords = reactive({ current: '', next: '', confirm: '' })
const changingPassword = ref(false)
const PASSWORD_MIN = 8

const newPasswordError = computed(() =>
  passwords.next && (passwords.next.length < PASSWORD_MIN || passwords.next.length > LIMITS.password)
    ? t('validation.password', { min: PASSWORD_MIN, max: LIMITS.password })
    : undefined
)
const confirmPasswordError = computed(() =>
  passwords.confirm && passwords.confirm !== passwords.next ? t('validation.passwordMismatch') : undefined
)
const canChangePassword = computed(() =>
  !!passwords.current && !!passwords.next && !!passwords.confirm
  && !newPasswordError.value && !confirmPasswordError.value
)

async function changePassword() {
  if (!canChangePassword.value) return
  changingPassword.value = true
  try {
    await auth.changePassword({ currentPassword: passwords.current, newPassword: passwords.next })
    Object.assign(passwords, { current: '', next: '', confirm: '' })
    success(t('settings.passwordSaved'))
  } catch (error) {
    if (statusOf(error) === 400) fail(t('settings.passwordWrong'))
    else notify(error)
  } finally {
    changingPassword.value = false
  }
}

async function logout() {
  await auth.logout()
  stars.reset()
  await navigateTo('/')
}
</script>

<template>
  <UContainer class="max-w-3xl py-8">
    <h1 class="mb-8 text-2xl font-bold text-highlighted">
      {{ t('settings.title') }}
    </h1>

    <div class="flex flex-col gap-8">
      <section class="rounded-lg bg-elevated/50 p-5">
        <h2 class="mb-4 font-semibold text-highlighted">
          {{ t('settings.appearance') }}
        </h2>
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormField :label="t('settings.language')">
            <LanguageSwitcher />
          </UFormField>
          <UFormField :label="t('settings.theme')">
            <USelect
              v-model="colorMode.preference"
              :items="themeItems"
              variant="soft"
              class="w-44"
            />
          </UFormField>
        </div>
      </section>

      <section class="rounded-lg bg-elevated/50 p-5">
        <h2 class="mb-4 font-semibold text-highlighted">
          {{ t('settings.profile') }}
        </h2>
        <div class="flex flex-col gap-6">
          <ImageUploader
            :label="t('settings.avatar')"
            :current-url="me?.avatarUrl"
            removable
            @uploaded="saveAvatar"
            @removed="saveAvatar(null)"
          />
          <UFormField :label="t('settings.bio')">
            <LazyMarkdownEditor
              v-model="bio"
              :rows="8"
            />
          </UFormField>
          <UButton
            :label="t('common.save')"
            :loading="savingBio"
            class="self-start"
            @click="saveBio"
          />
        </div>
      </section>

      <section class="rounded-lg bg-elevated/50 p-5">
        <h2 class="mb-4 font-semibold text-highlighted">
          {{ t('settings.account') }}
        </h2>
        <form
          class="flex flex-col gap-3 sm:flex-row sm:items-start"
          @submit.prevent="rename"
        >
          <UFormField
            :label="t('settings.username')"
            :description="t('settings.currentUsername', { username: auth.username })"
            :error="usernameError"
            class="flex-1"
          >
            <UInput
              v-model="newUsername"
              :placeholder="t('settings.newUsername')"
              autocomplete="username"
              variant="soft"
              class="w-full"
            />
          </UFormField>
          <UButton
            type="submit"
            :label="t('settings.changeUsername')"
            :loading="renaming"
            :disabled="!newUsername || !!usernameError"
            color="neutral"
            variant="soft"
            class="sm:mt-11"
          />
        </form>
        <USeparator class="my-5" />
        <form
          class="flex flex-col gap-3"
          @submit.prevent="changePassword"
        >
          <h3 class="font-medium text-highlighted">
            {{ t('settings.password') }}
          </h3>
          <!-- Hidden username lets password managers pair the new password with this account -->
          <input
            type="text"
            name="username"
            autocomplete="username"
            :value="auth.username"
            class="hidden"
            readonly
          >
          <UFormField :label="t('settings.currentPassword')">
            <UInput
              v-model="passwords.current"
              type="password"
              autocomplete="current-password"
              variant="soft"
              class="w-full sm:max-w-sm"
            />
          </UFormField>
          <UFormField
            :label="t('settings.newPassword')"
            :error="newPasswordError"
          >
            <UInput
              v-model="passwords.next"
              type="password"
              autocomplete="new-password"
              variant="soft"
              class="w-full sm:max-w-sm"
            />
          </UFormField>
          <UFormField
            :label="t('settings.confirmPassword')"
            :error="confirmPasswordError"
          >
            <UInput
              v-model="passwords.confirm"
              type="password"
              autocomplete="new-password"
              variant="soft"
              class="w-full sm:max-w-sm"
            />
          </UFormField>
          <UButton
            type="submit"
            :label="t('settings.changePassword')"
            :loading="changingPassword"
            :disabled="!canChangePassword"
            color="neutral"
            variant="soft"
            class="self-start"
          />
        </form>
        <USeparator class="my-5" />
        <UButton
          icon="i-lucide-log-out"
          :label="t('nav.logout')"
          color="error"
          variant="soft"
          @click="logout"
        />
      </section>
    </div>
  </UContainer>
</template>
