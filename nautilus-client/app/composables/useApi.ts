type FetchOptions = NonNullable<Parameters<typeof $fetch>[1]>

export type ApiOptions = FetchOptions & {
  /** Skip the Authorization header, e.g. for public endpoints */
  anonymous?: boolean
}

export function statusOf(error: unknown): number | undefined {
  return (error as { statusCode?: number } | null)?.statusCode
}

/**
 * Fetch wrapper for nautilus-server: attaches the bearer token and, on 401,
 * refreshes the token once and retries the request.
 */
export function useApi() {
  const auth = useAuthStore()
  const apiBase = useRuntimeConfig().public.apiBase

  async function request<T>(url: string, options: ApiOptions = {}): Promise<T> {
    const { anonymous, ...fetchOptions } = options

    const send = () => {
      const headers = new Headers(fetchOptions.headers)
      if (!anonymous && auth.accessToken) {
        headers.set('Authorization', `Bearer ${auth.accessToken}`)
      }
      return $fetch<T>(url, {
        ...fetchOptions,
        baseURL: apiBase,
        credentials: 'include',
        headers
      }) as Promise<T>
    }

    try {
      return await send()
    } catch (error) {
      if (!anonymous && statusOf(error) === 401 && auth.accessToken && await auth.refresh()) {
        return await send()
      }
      throw error
    }
  }

  return request
}
