import { defineStore } from 'pinia'
import type { AccessTokenResponse, ChangePasswordRequest, LoginRequest, RegisterRequest } from '~/types/api'

export const useAuthStore = defineStore('auth', () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  // Kept in memory only; the refresh token lives in an HttpOnly cookie
  const accessToken = ref<string | null>(null)
  const ready = ref(false)

  const claims = computed(() => accessToken.value ? decodeJwt(accessToken.value) : null)
  const username = computed(() => claims.value?.sub ?? null)
  const role = computed(() => claims.value?.role ?? null)
  const loggedIn = computed(() => !!accessToken.value)

  let refreshing: Promise<boolean> | null = null
  let restoring: Promise<void> | null = null

  function clear() {
    accessToken.value = null
  }

  /** Exchanges the refresh cookie for a new access token. Concurrent callers share one request. */
  function refresh(): Promise<boolean> {
    refreshing ??= $fetch<AccessTokenResponse>('/auth/refresh', {
      baseURL: apiBase,
      method: 'POST',
      credentials: 'include'
    })
      .then((res) => {
        accessToken.value = res.accessToken
        return true
      })
      .catch(() => {
        clear()
        return false
      })
      .finally(() => {
        refreshing = null
      })
    return refreshing
  }

  /** Restores the session from the refresh cookie once per page load; later callers wait for the same attempt. */
  function restore(): Promise<void> {
    restoring ??= refresh().then(() => {
      ready.value = true
    })
    return restoring
  }

  async function login(body: LoginRequest) {
    const res = await $fetch<AccessTokenResponse>('/auth/login', {
      baseURL: apiBase,
      method: 'POST',
      credentials: 'include',
      body
    })
    accessToken.value = res.accessToken
  }

  async function register(body: RegisterRequest) {
    await $fetch('/auth/register', { baseURL: apiBase, method: 'POST', body })
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await $fetch('/auth/logout', {
          baseURL: apiBase,
          method: 'POST',
          credentials: 'include',
          headers: { Authorization: `Bearer ${accessToken.value}` }
        })
      }
    } finally {
      clear()
    }
  }

  /** The backend revokes all sessions and returns a fresh token pair for this one. */
  async function changePassword(body: ChangePasswordRequest) {
    const res = await useApi()<AccessTokenResponse>('/auth/password', { method: 'PUT', body })
    accessToken.value = res.accessToken
  }

  return {
    accessToken,
    ready,
    username,
    role,
    loggedIn,
    refresh,
    restore,
    login,
    register,
    logout,
    changePassword,
    clear
  }
})
