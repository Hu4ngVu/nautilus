<script setup lang="ts">
const { t } = useI18n()
</script>

<template>
  <UContainer class="grid min-h-[calc(100svh-var(--ui-header-height)-4rem)] items-center gap-12 py-10 lg:grid-cols-2">
    <div
      class="hidden flex-col items-start gap-6 lg:flex"
      aria-hidden="true"
    >
      <PixelShell :size="112" />
      <p class="max-w-md font-pixel text-pixel-4xl text-highlighted">
        <i18n-t keypath="home.taglineRich">
          <template #highlight>
            <span class="text-primary">{{ t('home.taglineHighlight') }}</span>
          </template>
        </i18n-t>
      </p>
      <p class="max-w-sm text-muted">
        {{ t('auth.panelTagline') }}
      </p>
    </div>

    <div class="mx-auto w-full max-w-md p-6 block-surface block-raised lift-primary sm:p-8">
      <slot />
    </div>
  </UContainer>
</template>
