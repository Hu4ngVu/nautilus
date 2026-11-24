// Restore the session from the refresh cookie
export default defineNuxtPlugin(async (nuxtApp) => {
  const auth = useAuthStore()
  if (nuxtApp.isHydrating && nuxtApp.payload.serverRendered) {
    // The server rendered as a guest; logging in before hydration would make the first render differ from its HTML
    nuxtApp.hooks.hookOnce('app:suspense:resolve', () => {
      auth.restore()
    })
  } else {
    await auth.restore()
  }
})
