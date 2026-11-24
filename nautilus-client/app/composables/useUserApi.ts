import type { UpdateUserInfoRequest, UserInfo } from '~/types/api'

export function useUserApi() {
  const api = useApi()
  const apiBase = useRuntimeConfig().public.apiBase

  return {
    get: (username: string) =>
      api<UserInfo>(`/users/${encodeURIComponent(username)}`),

    update: (username: string, body: UpdateUserInfoRequest) =>
      api(`/users/${encodeURIComponent(username)}`, { method: 'PATCH', body }),

    rename: (username: string, newUsername: string) =>
      api(`/users/${encodeURIComponent(username)}`, { method: 'PUT', body: { newUsername } }),

    usernameTaken: (username: string) =>
      $fetch<boolean>('/auth/check-username', { baseURL: apiBase, method: 'POST', query: { username } }),

    emailTaken: (email: string) =>
      $fetch<boolean>('/auth/check-email', { baseURL: apiBase, method: 'POST', query: { email } })
  }
}
