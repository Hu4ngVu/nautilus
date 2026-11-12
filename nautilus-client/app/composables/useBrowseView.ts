export type BrowseView = 'list' | 'grid'

const STORAGE_KEY = 'nautilus:browse-view'

// Storage can throw (private mode, blocked site data); the list view is always a safe default
function readView(): BrowseView {
  try {
    return localStorage.getItem(STORAGE_KEY) === 'grid' ? 'grid' : 'list'
  } catch {
    return 'list'
  }
}

/** List/grid choice for Browse, remembered per browser. */
export function useBrowseView() {
  const view = useState<BrowseView>('browse-view', readView)
  watch(view, (value) => {
    try {
      localStorage.setItem(STORAGE_KEY, value)
    } catch {
      // Not persisted; the choice still holds for this session
    }
  })
  return view
}
