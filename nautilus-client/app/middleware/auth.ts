export default defineNuxtRouteMiddleware((to) => {
  const auth = useAuthStore()
  if (!auth.loggedIn) {
    return navigateTo({ path: '/login', query: { redirect: to.fullPath } })
  }
})
