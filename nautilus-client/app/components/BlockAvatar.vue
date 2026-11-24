<script setup lang="ts">
// Placeholder for a missing or broken avatar/banner: a shaded block coloured from the name
const props = withDefaults(defineProps<{
  name: string
  shape?: 'square' | 'banner'
}>(), {
  shape: 'square'
})

const color = computed(() => blockColor(props.name))
const initial = computed(() => Array.from(props.name.trim())[0]?.toUpperCase() ?? '?')

const style = computed(() => {
  const c = color.value
  return props.shape === 'banner'
    ? { backgroundImage: `repeating-linear-gradient(45deg, ${c.shade} 0 14px, ${c.face} 14px 28px)` }
    : {
        backgroundColor: c.face,
        color: c.ink,
        boxShadow: `inset -0.35em -0.35em 0 ${c.shade}, inset 0.25em 0.25em 0 ${c.light}`
      }
})
</script>

<template>
  <div
    class="flex size-full items-center justify-center overflow-hidden [container-type:size]"
    role="img"
    :aria-label="name"
    :style="style"
  >
    <span
      v-if="shape === 'square'"
      class="font-pixel text-[55cqmin] leading-none select-none"
      aria-hidden="true"
    >{{ initial }}</span>
  </div>
</template>
