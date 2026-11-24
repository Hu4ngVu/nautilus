/** Shows a toast for an API error using a status-specific, localized message. */
export function useApiError() {
  const toast = useToast()
  const { t } = useI18n()

  function messageFor(error: unknown): string {
    if (error instanceof UploadValidationError) {
      return t(`errors.upload.${error.reason}`)
    }
    const status = statusOf(error)
    if (status === 400) {
      const data = (error as { data?: unknown }).data
      if (typeof data === 'string' && data.length < 200) return data
      return t('errors.badRequest')
    }
    if (status === 401) return t('errors.unauthorized')
    if (status === 403) return t('errors.forbidden')
    if (status === 404) return t('errors.notFound')
    if (status === 409) return t('errors.conflict')
    return t('errors.generic')
  }

  function notify(error: unknown, title?: string) {
    toast.add({
      title: title ?? t('errors.title'),
      description: messageFor(error),
      color: 'error',
      icon: 'i-lucide-circle-alert'
    })
  }

  function success(title: string) {
    toast.add({ title, color: 'success', icon: 'i-lucide-check' })
  }

  function fail(title: string) {
    toast.add({ title, color: 'error', icon: 'i-lucide-circle-alert' })
  }

  return { notify, success, fail, messageFor }
}
