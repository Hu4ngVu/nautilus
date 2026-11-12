/**
 * Whether the current user can edit a server. Owners are known from the token;
 * for other users we probe the editor-only tags endpoint.
 */
export function useServerPermission(username: MaybeRefOrGetter<string>, servername: MaybeRefOrGetter<string>) {
  const auth = useAuthStore()
  const servers = useServerApi()

  const isOwner = computed(() => !!auth.username && auth.username === toValue(username))
  const isEditor = ref(false)

  watchEffect(async () => {
    const u = toValue(username)
    const s = toValue(servername)
    isEditor.value = false
    if (!auth.loggedIn || isOwner.value) return
    try {
      await servers.tags(u, s)
      isEditor.value = true
    } catch {
      isEditor.value = false
    }
  })

  const canEdit = computed(() => isOwner.value || isEditor.value)

  return { isOwner, canEdit }
}
