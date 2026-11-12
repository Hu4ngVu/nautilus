<script setup lang="ts">
import type { DropdownMenuItem } from '@nuxt/ui'

const { t } = useI18n()
const auth = useAuthStore()
const stars = useStarsStore()
const users = useUserApi()

const { data: me } = useAsyncData('me', () => auth.username ? users.get(auth.username) : Promise.resolve(null), {
  watch: [() => auth.username]
})

async function logout() {
  await auth.logout()
  stars.reset()
  await navigateTo('/')
}

const items = computed<DropdownMenuItem[][]>(() => [
  [{ label: auth.username ?? '', type: 'label' }],
  [
    { label: t('nav.profile'), icon: 'i-lucide-user', to: `/u/${encodeURIComponent(auth.username ?? '')}` },
    { label: t('nav.stars'), icon: 'i-lucide-star', to: '/stars' },
    { label: t('nav.settings'), icon: 'i-lucide-settings', to: '/settings' }
  ],
  [{ label: t('nav.logout'), icon: 'i-lucide-log-out', onSelect: logout }]
])
</script>

<template>
  <UDropdownMenu
    :items="items"
    :content="{ align: 'end' }"
  >
    <UButton
      color="neutral"
      variant="ghost"
      square
      :aria-label="t('nav.account')"
    >
      <UAvatar
        :src="me?.avatarUrl ?? undefined"
        :alt="auth.username ?? undefined"
        size="sm"
      />
    </UButton>
  </UDropdownMenu>
</template>
