import type { ServerPlatform } from '~/types/api'

export const PLATFORM_LABEL: Record<ServerPlatform, string> = {
  JAVA: 'Java',
  BEDROCK: 'Bedrock'
}

export function serverPath(username: string, servername: string) {
  return `/${encodeURIComponent(username)}/${encodeURIComponent(servername)}`
}
