export default defineNuxtRouteMiddleware(async (to) => {
  const auth = useAuthStore()
  // Leaving a server-rendered page can happen before the session is restored
  await auth.restore()
  if (!auth.loggedIn) {
    return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
  }
})
