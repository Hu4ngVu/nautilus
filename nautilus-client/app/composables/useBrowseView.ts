export type BrowseView = 'list' | 'grid'

/** List/grid choice for Browse, remembered per browser in a cookie so server-rendered pages use it too. */
export function useBrowseView() {
  const cookie = useCookie<string | null>('nautilus-browse-view', { maxAge: 60 * 60 * 24 * 365, sameSite: 'lax' })
  return computed<BrowseView>({
    get: () => cookie.value === 'grid' ? 'grid' : 'list',
    set: (value) => {
      cookie.value = value
    }
  })
}
