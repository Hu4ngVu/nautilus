import type { Ref } from 'vue'

export type Availability = 'idle' | 'checking' | 'available' | 'taken' | 'error'

/**
 * Live "is this username/email free?" hint while typing. Advisory only: the server
 * still validates on submit. Results for stale input are dropped.
 */
export function useAvailability(kind: 'username' | 'email', value: Ref<string>, isValid: (v: string) => boolean) {
  const users = useUserApi()
  const status = ref<Availability>('idle')
  let timer: ReturnType<typeof setTimeout> | undefined

  watch(value, (current) => {
    clearTimeout(timer)
    if (!current || !isValid(current)) {
      status.value = 'idle'
      return
    }
    status.value = 'checking'
    timer = setTimeout(async () => {
      try {
        const taken = kind === 'username' ? await users.usernameTaken(current) : await users.emailTaken(current)
        if (value.value === current) status.value = taken ? 'taken' : 'available'
      } catch {
        if (value.value === current) status.value = 'error'
      }
    }, 400)
  })

  onBeforeUnmount(() => clearTimeout(timer))
  return status
}
