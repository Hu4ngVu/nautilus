<script setup lang="ts">
const props = defineProps<{
  username: string
  servername: string
}>()

const count = defineModel<number>('count', { default: 0 })

const { t, n } = useI18n()
const auth = useAuthStore()
const stars = useStarsStore()
const route = useRoute()
const { notify } = useApiError()

const pending = ref(false)
const starred = computed(() => stars.isStarred(props.username, props.servername))

onMounted(() => {
  if (auth.loggedIn) stars.load().catch(() => {})
})

async function toggle() {
  if (!auth.loggedIn) {
    await navigateTo({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  pending.value = true
  try {
    const nowStarred = await stars.toggle(props.username, props.servername)
    count.value += nowStarred ? 1 : -1
  } catch (error) {
    notify(error)
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <UButton
    :icon="starred ? 'i-lucide-star-off' : 'i-lucide-star'"
    :label="`${starred ? t('server.unstar') : t('server.star')} · ${n(count)}`"
    :color="starred ? 'neutral' : 'primary'"
    :variant="starred ? 'soft' : 'solid'"
    :loading="pending"
    :aria-pressed="starred"
    @click="toggle"
  />
</template>
