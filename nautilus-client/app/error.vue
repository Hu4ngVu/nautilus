<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{ error: NuxtError }>()
const { t } = useI18n()

const notFound = computed(() => props.error.statusCode === 404)

useSeoMeta({ title: () => notFound.value ? t('errors.pageNotFound') : t('errors.title') })

function goHome() {
  clearError({ redirect: '/' })
}
</script>

<template>
  <UApp>
    <UMain class="flex min-h-svh items-center justify-center px-4 py-16">
      <div class="flex flex-col items-center gap-6 text-center">
        <PixelShell :size="72" />
        <p class="font-pixel text-[6rem] leading-none text-highlighted sm:text-[8rem]">
          {{ error.statusCode }}
        </p>
        <div class="flex max-w-md flex-col gap-2">
          <h1 class="font-pixel text-pixel-2xl text-highlighted">
            {{ notFound ? t('errors.pageNotFound') : t('errors.title') }}
          </h1>
          <p class="text-muted">
            {{ notFound ? t('errors.pageNotFoundHint') : t('errors.generic') }}
          </p>
        </div>
        <UButton
          icon="i-lucide-house"
          :label="t('errors.backHome')"
          size="lg"
          @click="goHome"
        />
      </div>
    </UMain>
  </UApp>
</template>
