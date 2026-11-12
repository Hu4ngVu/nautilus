<script setup lang="ts">
import type { NavigationMenuItem } from '@nuxt/ui'
import { en, vi } from '@nuxt/ui/locale'

const { locale, t } = useI18n()
const auth = useAuthStore()

const uiLocale = computed(() => locale.value === 'en' ? en : vi)

useHead({
  htmlAttrs: { lang: locale },
  titleTemplate: title => title ? `${title} · Nautilus` : 'Nautilus'
})

useSeoMeta({
  description: () => t('app.description')
})

const navItems = computed<NavigationMenuItem[]>(() => [
  { label: t('nav.browse'), to: '/browse', icon: 'i-lucide-compass' },
  { label: t('nav.dashboard'), to: '/dashboard', icon: 'i-lucide-layout-dashboard' }
])
</script>

<template>
  <UApp :locale="uiLocale">
    <UHeader>
      <template #left>
        <NuxtLink
          to="/"
          class="rounded-md p-1 -ms-1 focus-visible:outline-3 outline-primary/40"
          :aria-label="t('nav.home')"
        >
          <AppLogo />
        </NuxtLink>
      </template>

      <UNavigationMenu
        :items="navItems"
        variant="link"
        highlight
      />

      <template #right>
        <UColorModeButton />
        <UserMenu v-if="auth.loggedIn" />
        <UButton
          v-else
          to="/login"
          :label="t('nav.login')"
          color="neutral"
          variant="soft"
        />
      </template>

      <template #body>
        <UNavigationMenu
          :items="navItems"
          orientation="vertical"
          class="-mx-2.5"
        />
      </template>
    </UHeader>

    <UMain>
      <NuxtPage />
    </UMain>

    <UFooter>
      <template #left>
        <p class="text-sm text-muted">
          Nautilus © {{ new Date().getFullYear() }}
        </p>
      </template>
      <template #right>
        <LanguageSwitcher />
      </template>
    </UFooter>
  </UApp>
</template>
