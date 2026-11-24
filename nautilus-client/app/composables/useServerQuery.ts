import type { ServerFilters, ServerPlatform, ServerType } from '~/types/api'

const PAGE_SIZE = 18

export type SortKey = 'starCount,desc' | 'starCount,asc'

export interface ServerQueryState extends ServerFilters {
  /** Search text; empty means the plain listing */
  query?: string
  page: number
  sort: SortKey
}

function str(v: unknown) {
  return typeof v === 'string' && v ? v : undefined
}

/** Two-way binding between the server list filters and the URL query string. */
export function useServerQuery() {
  const route = useRoute()
  const router = useRouter()

  const state = computed<ServerQueryState>(() => {
    const q = route.query
    const premium = str(q.premium)
    const tags = q.tags
    return {
      query: str(q.query)?.trim() || undefined,
      page: Math.max(1, Number(q.page) || 1),
      sort: (str(q.sort) as SortKey) ?? 'starCount,desc',
      serverType: str(q.type) as ServerType | undefined,
      serverPlatform: str(q.platform) as ServerPlatform | undefined,
      requiresPremium: premium === undefined ? undefined : premium === 'true',
      tags: (Array.isArray(tags) ? tags : tags ? [tags] : []).filter((t): t is string => !!t)
    }
  })

  function update(patch: Partial<ServerQueryState>) {
    const next = { ...state.value, page: 1, ...patch }
    router.replace({
      query: {
        query: next.query?.trim() || undefined,
        page: next.page > 1 ? String(next.page) : undefined,
        sort: next.sort !== 'starCount,desc' ? next.sort : undefined,
        type: next.serverType,
        platform: next.serverPlatform,
        premium: next.requiresPremium === undefined ? undefined : String(next.requiresPremium),
        tags: next.tags?.length ? next.tags : undefined
      }
    })
  }

  /** Request params for the backend (0-based page). */
  const params = computed(() => ({
    page: state.value.page - 1,
    size: PAGE_SIZE,
    sort: state.value.sort,
    serverType: state.value.serverType,
    serverPlatform: state.value.serverPlatform,
    requiresPremium: state.value.requiresPremium,
    tags: state.value.tags?.length ? state.value.tags : undefined
  }))

  return { state, update, params, pageSize: PAGE_SIZE }
}
