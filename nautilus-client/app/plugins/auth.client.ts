// Restore the session from the refresh cookie before the first navigation
export default defineNuxtPlugin(async () => {
  const auth = useAuthStore()
  await auth.refresh()
  auth.ready = true
})
