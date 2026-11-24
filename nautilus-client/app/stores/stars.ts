import { defineStore } from 'pinia'

const key = (username: string, servername: string) => `${username}/${servername}`

/**
 * Tracks which servers the current user has starred. The backend has no
 * "did I star this" endpoint, so the user's starred list is loaded once per session.
 */
export const useStarsStore = defineStore('stars', () => {
  const auth = useAuthStore()
  const starred = ref(new Set<string>())
  let loadedFor: string | null = null
  let loading: Promise<void> | null = null

  async function load() {
    const me = auth.username
    if (!me || loadedFor === me) return
    loading ??= (async () => {
      const api = useServerApi()
      const next = new Set<string>()
      let page = 0
      let last = false
      while (!last) {
        const res = await api.starred(me, { page, size: 100 })
        res.content.forEach(s => next.add(key(s.username, s.servername)))
        last = res.page.number + 1 >= res.page.totalPages || res.content.length === 0
        page++
      }
      starred.value = next
      loadedFor = me
    })().finally(() => {
      loading = null
    })
    return loading
  }

  function isStarred(username: string, servername: string) {
    return starred.value.has(key(username, servername))
  }

  async function toggle(username: string, servername: string) {
    const api = useServerApi()
    const k = key(username, servername)
    if (starred.value.has(k)) {
      await api.unstar(username, servername)
      starred.value.delete(k)
      return false
    }
    await api.star(username, servername)
    starred.value.add(k)
    return true
  }

  async function star(username: string, servername: string) {
    await useServerApi().star(username, servername)
    starred.value.add(key(username, servername))
  }

  async function unstar(username: string, servername: string) {
    await useServerApi().unstar(username, servername)
    starred.value.delete(key(username, servername))
  }

  function reset() {
    starred.value = new Set()
    loadedFor = null
  }

  return { load, isStarred, toggle, star, unstar, reset }
})
