import type {
  BedrockVersions,
  CreateServerRequest,
  GalleryImage,
  GalleryImageDetail,
  GalleryImageRequest,
  JavaVersions,
  Page,
  PageParams,
  ServerDetail,
  ServerFilters,
  ServerStargazer,
  ServerSummary,
  StarredServer,
  TagMap,
  TagResponse,
  UpdateServerRequest
} from '~/types/api'

const seg = encodeURIComponent

function serverPath(username: string, servername: string) {
  return `${seg(username)}/${seg(servername)}`
}

export function useServerApi() {
  const api = useApi()

  return {
    list: (filters: ServerFilters & PageParams) =>
      api<Page<ServerSummary>>('/servers', { query: filters }),

    search: (query: string, filters: ServerFilters & PageParams) =>
      api<Page<ServerSummary>>('/servers/search', { query: { ...filters, query } }),

    listByUser: (username: string, params: PageParams = {}) =>
      api<Page<ServerSummary>>(`/servers/${seg(username)}`, { query: params }),

    get: (username: string, servername: string) =>
      api<ServerDetail>(`/servers/${serverPath(username, servername)}`),

    create: (username: string, body: CreateServerRequest) =>
      api(`/servers/${seg(username)}`, { method: 'POST', body }),

    update: (username: string, servername: string, body: UpdateServerRequest) =>
      api(`/servers/${serverPath(username, servername)}`, { method: 'PATCH', body }),

    rename: (username: string, servername: string, newServername: string) =>
      api(`/servers/${serverPath(username, servername)}`, {
        method: 'PUT',
        query: { newServername }
      }),

    setVisibility: (username: string, servername: string, visibility: boolean) =>
      api(`/servers/${serverPath(username, servername)}/visibility`, {
        method: 'PUT',
        body: { visibility }
      }),

    remove: (username: string, servername: string) =>
      api(`/servers/${serverPath(username, servername)}`, { method: 'DELETE' }),

    // Gallery
    gallery: (username: string, servername: string, params: PageParams = {}) =>
      api<Page<GalleryImage>>(`/servers/gallery/${serverPath(username, servername)}`, { query: params }),

    galleryImage: (username: string, servername: string, imageId: string) =>
      api<GalleryImageDetail>(`/servers/gallery/${serverPath(username, servername)}/${seg(imageId)}`),

    addImage: (username: string, servername: string, body: GalleryImageRequest) =>
      api(`/servers/gallery/${serverPath(username, servername)}`, { method: 'POST', body }),

    updateImage: (username: string, servername: string, imageId: string, body: GalleryImageRequest) =>
      api(`/servers/gallery/${serverPath(username, servername)}/${seg(imageId)}`, { method: 'PATCH', body }),

    removeImage: (username: string, servername: string, imageId: string) =>
      api(`/servers/gallery/${serverPath(username, servername)}/${seg(imageId)}`, { method: 'DELETE' }),

    // Tags (editor only)
    tags: (username: string, servername: string) =>
      api<TagResponse>(`/servers/tags/${serverPath(username, servername)}`),

    setTags: (username: string, servername: string, tags: TagMap) =>
      api(`/servers/tags/${serverPath(username, servername)}`, { method: 'PUT', body: { tags } }),

    // Minecraft versions
    javaVersions: (username: string, servername: string) =>
      api<JavaVersions>(`/servers/versions/java/${serverPath(username, servername)}`),

    setJavaVersions: (username: string, servername: string, body: JavaVersions) =>
      api(`/servers/versions/java/${serverPath(username, servername)}`, { method: 'PUT', body }),

    bedrockVersions: (username: string, servername: string) =>
      api<BedrockVersions>(`/servers/versions/bedrock/${serverPath(username, servername)}`),

    setBedrockVersions: (username: string, servername: string, body: BedrockVersions) =>
      api(`/servers/versions/bedrock/${serverPath(username, servername)}`, { method: 'PUT', body }),

    // Stars
    stargazers: (username: string, servername: string, params: PageParams = {}) =>
      api<Page<ServerStargazer>>(`/stars/${serverPath(username, servername)}`, { query: params }),

    starred: (username: string, params: PageParams = {}) =>
      api<Page<StarredServer>>(`/stars/${seg(username)}`, { query: params }),

    star: (username: string, servername: string) =>
      api(`/stars/${serverPath(username, servername)}`, { method: 'PUT' }),

    unstar: (username: string, servername: string) =>
      api(`/stars/${serverPath(username, servername)}`, { method: 'DELETE' })
  }
}
