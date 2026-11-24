<script setup lang="ts">
// Nautilus shell as 8x8 pixel art; 0 = empty, 1 = face, 2 = shade, 3 = highlight, 4 = deep
withDefaults(defineProps<{ size?: number }>(), { size: 32 })

const GRID = [
  [0, 0, 2, 1, 1, 2, 0, 0],
  [0, 2, 3, 1, 1, 1, 2, 0],
  [2, 1, 1, 4, 4, 1, 1, 2],
  [2, 1, 4, 3, 1, 4, 1, 2],
  [2, 1, 4, 1, 4, 4, 1, 2],
  [0, 2, 1, 4, 1, 1, 2, 0],
  [0, 0, 2, 1, 1, 2, 2, 0],
  [0, 0, 0, 2, 2, 0, 0, 0]
]

const FILL = ['', 'var(--color-nautilus-500)', 'var(--color-nautilus-700)', 'var(--color-nautilus-300)', 'var(--color-nautilus-900)']

const cells = GRID.flatMap((row, y) => row.map((v, x) => ({ x, y, v }))).filter(c => c.v)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 8 8"
    shape-rendering="crispEdges"
    role="img"
    aria-label="Nautilus"
    class="shrink-0"
  >
    <rect
      v-for="c in cells"
      :key="`${c.x}-${c.y}`"
      :x="c.x"
      :y="c.y"
      width="1"
      height="1"
      :fill="FILL[c.v]"
    />
  </svg>
</template>
