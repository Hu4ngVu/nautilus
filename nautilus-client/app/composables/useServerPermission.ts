/**
 * Whether the current user can edit a server. Owners are known from the token;
 * for other users we probe the editor-only tags endpoint.
 */
export function useServerPermission(username: MaybeRefOrGetter<string>, servername: MaybeRefOrGetter<string>) {
  const auth = useAuthStore()
  const servers = useServerApi()

  const isOwner = computed(() => !!auth.username && auth.username === toValue(username))
  const isEditor = ref(false)
  // False while the editor probe is in flight, so callers can tell "no" from "not known yet"
  const resolved = ref(false)

  watchEffect(async () => {
    const u = toValue(username)
    const s = toValue(servername)
    isEditor.value = false
    if (!auth.loggedIn || isOwner.value) {
      resolved.value = true
      return
    }
    resolved.value = false
    try {
      await servers.tags(u, s)
      isEditor.value = true
    } catch {
      isEditor.value = false
    } finally {
      // A newer probe for another server may have started meanwhile; only it may resolve
      if (toValue(username) === u && toValue(servername) === s) resolved.value = true
    }
  })

  const canEdit = computed(() => isOwner.value || isEditor.value)

  return { isOwner, canEdit, resolved }
}
