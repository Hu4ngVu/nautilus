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
  { label: t('settings.themeLight'), description: t('settings.themeLightHint'), value: 'light', icon: 'i-lucide-sun' },
  { label: t('settings.themeDark'), description: t('settings.themeDarkHint'), value: 'dark', icon: 'i-lucide-moon' },
  { label: t('settings.themeSystem'), description: t('settings.themeSystemHint'), value: 'system', icon: 'i-lucide-monitor' }
])

type Section = 'profile' | 'account' | 'appearance'

const route = useRoute()
const router = useRouter()

const section = computed<Section>({
  get: () => {
    const q = route.query.section
    return q === 'account' || q === 'appearance' ? q : 'profile'
  },
  set: (value) => {
    router.replace({ query: { ...route.query, section: value === 'profile' ? undefined : value } })
  }
})

const sections = computed(() => [
  { value: 'profile' as const, label: t('settings.profile'), icon: 'i-lucide-user' },
  { value: 'account' as const, label: t('settings.account'), icon: 'i-lucide-key-round' },
  { value: 'appearance' as const, label: t('settings.appearance'), icon: 'i-lucide-palette' }
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
  <UContainer class="py-8">
    <PageHeader :title="t('settings.title')" />

    <div class="grid gap-8 md:grid-cols-[14rem_1fr]">
      <nav
        :aria-label="t('settings.title')"
        class="-mx-4 flex gap-2 overflow-x-auto px-4 pb-2 md:mx-0 md:flex-col md:self-start md:overflow-visible md:px-0 md:sticky md:top-[calc(var(--ui-header-height)+1.5rem)]"
      >
        <UButton
          v-for="item in sections"
          :key="item.value"
          :icon="item.icon"
          :label="item.label"
          :aria-current="section === item.value ? 'page' : undefined"
          :color="section === item.value ? 'primary' : 'neutral'"
          :variant="section === item.value ? 'solid' : 'ghost'"
          class="shrink-0 md:w-full"
          @click="section = item.value"
        />
      </nav>

      <section
        v-if="section === 'profile'"
        class="min-w-0 p-5 block-surface sm:p-6"
      >
        <h2 class="mb-5 font-pixel text-pixel-2xl text-highlighted">
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

      <section
        v-else-if="section === 'account'"
        class="min-w-0 p-5 block-surface sm:p-6"
      >
        <h2 class="mb-5 font-pixel text-pixel-2xl text-highlighted">
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
          <h3 class="font-pixel text-pixel-lg text-highlighted">
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
        <div class="flex flex-wrap items-center justify-between gap-3 rounded-lg border-2 border-red-500 p-4 shadow-[4px_4px_0_var(--color-red-500)]">
          <p class="text-sm text-toned">
            {{ t('settings.logoutHint') }}
          </p>
          <UButton
            icon="i-lucide-log-out"
            :label="t('nav.logout')"
            color="error"
            variant="soft"
            @click="logout"
          />
        </div>
      </section>

      <section
        v-else
        class="min-w-0 p-5 block-surface sm:p-6"
      >
        <h2 class="mb-5 font-pixel text-pixel-2xl text-highlighted">
          {{ t('settings.appearance') }}
        </h2>
        <div class="grid gap-6 sm:grid-cols-2">
          <UFormField :label="t('settings.language')">
            <LanguageSwitcher />
          </UFormField>
          <UFormField
            :label="t('settings.theme')"
            class="sm:col-span-2"
          >
            <URadioGroup
              v-model="colorMode.preference"
              :items="themeItems"
              variant="card"
              orientation="horizontal"
              indicator="hidden"
              :ui="{ fieldset: 'grid gap-3 sm:grid-cols-3', item: 'items-start gap-3 rounded-lg border-2 border-(--block-border) ring-0 bg-(--block-surface) has-data-[state=checked]:border-nautilus-500 has-data-[state=checked]:shadow-[4px_4px_0_var(--color-nautilus-500)]', label: 'font-pixel text-pixel-lg' }"
            />
          </UFormField>
        </div>
      </section>
    </div>
  </UContainer>
</template>
