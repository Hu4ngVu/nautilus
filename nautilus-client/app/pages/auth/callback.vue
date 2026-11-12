<script setup lang="ts">
// Landing page after Google OAuth: the backend has set the refresh cookie
const { t } = useI18n()
const route = useRoute()
const auth = useAuthStore()

useSeoMeta({ title: () => t('auth.callback.title') })

const failed = ref(typeof route.query.error === 'string')

onMounted(async () => {
  if (failed.value) return
  if (await auth.refresh()) {
    await navigateTo('/', { replace: true })
  } else {
    failed.value = true
  }
})
</script>

<template>
  <UContainer class="flex justify-center py-16">
    <EmptyState
      v-if="failed"
      icon="i-lucide-circle-alert"
      :title="t('auth.callback.failed')"
      :description="t('auth.callback.failedHint')"
    >
      <UButton
        to="/login"
        :label="t('auth.login.link')"
        class="mt-2"
      />
    </EmptyState>
    <div
      v-else
      class="flex items-center gap-3 text-muted"
      role="status"
    >
      <UIcon
        name="i-lucide-loader-circle"
        class="size-5 animate-spin"
      />
      {{ t('auth.callback.loading') }}
    </div>
  </UContainer>
</template>
