<script setup lang="ts">
const props = defineProps<{ source?: string | null }>()

const html = ref('')

watch(() => props.source, async (source) => {
  html.value = source ? await renderMarkdown(source) : ''
}, { immediate: true })
</script>

<template>
  <!-- `markdown` is defined in main.css; html is sanitized with DOMPurify -->
  <!-- eslint-disable-next-line better-tailwindcss/no-unknown-classes, vue/no-v-html, vue/max-attributes-per-line -->
  <div class="markdown" v-html="html" />
</template>
