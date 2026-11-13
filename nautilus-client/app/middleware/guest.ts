export default defineNuxtRouteMiddleware(async () => {
  const auth = useAuthStore()
  await auth.restore()
  if (auth.loggedIn) return navigateTo('/')
})
