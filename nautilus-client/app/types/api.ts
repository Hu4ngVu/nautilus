export type ServerType = 'VANILLA' | 'MODPACK'
export type ServerPlatform = 'JAVA' | 'BEDROCK'
export type TagCategory = 'FEATURES' | 'GAMEPLAY' | 'META' | 'COMMUNITY'

/** Spring Data page response (PagedModel): the paging numbers are nested under `page` */
export interface Page<T> {
  content: T[]
  page: PageMetadata
}

export interface PageMetadata {
  size: number
  /** 0-based */
  number: number
  totalElements: number
  totalPages: number
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
}

export interface AccessTokenResponse {
  accessToken: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface UserInfo {
  username: string
  serverCount: number
  bio: string | null
  avatarUrl: string | null
}

export interface UpdateUserInfoRequest {
  resourceId?: string | null
  bio?: string | null
}

export interface ServerSummary {
  username: string
  servername: string
  requiresPremium: boolean
  avatarUrl: string | null
  bannerUrl: string | null
  summary: string
  starCount: number
  visibility: boolean
  serverType: ServerType
  serverPlatform: ServerPlatform
  /** First three tags only */
  tags: string[]
  primaryVersion: string | null
}

export interface ServerDetail {
  servername: string
  visibility: boolean
  requiresPremium: boolean
  javaAddress: string | null
  bedrockAddress: string | null
  avatarUrl: string | null
  bannerUrl: string | null
  summary: string
  bio: string | null
  tags: string[]
  categories: string[]
  javaVersions: string[]
  bedrockVersions: string[]
  serverType: ServerType
  serverPlatform: ServerPlatform
  starCount: number
}

export interface ServerFilters {
  requiresPremium?: boolean
  serverType?: ServerType
  serverPlatform?: ServerPlatform
  tags?: string[]
}

export interface CreateServerRequest {
  servername: string
  visibility: boolean
  requiresPremium: boolean
  type: ServerType
  platform: ServerPlatform
  summary: string
}

export interface UpdateServerRequest {
  avatarResourceId?: string | null
  bannerResourceId?: string | null
  requiresPremium?: boolean
  javaAddress?: string | null
  bedrockAddress?: string | null
  summary?: string | null
  bio?: string | null
}

export interface GalleryImage {
  imageId: string
  imageUrl: string
  imageTitle: string | null
  orderIndex: number
  createdAt: string
}

export interface GalleryImageDetail extends GalleryImage {
  summary: string | null
}

export interface GalleryImageRequest {
  resourceId?: string
  title?: string | null
  summary?: string | null
  orderIndex?: number | null
}

export type TagMap = Partial<Record<TagCategory, string[]>>

export interface TagResponse {
  tags: TagMap
}

/** Major version -> set of patch versions, e.g. { "1.20": ["1.20.1"] } */
export type VersionMap = Record<string, string[]>

export interface JavaVersions {
  javaVersions: VersionMap
  primaryJavaVersion: string | null
}

export interface BedrockVersions {
  bedrockVersions: VersionMap
  primaryBedrockVersion: string | null
}

export interface ServerStargazer {
  username: string
  avatarUrl: string | null
}

export interface StarredServer {
  username: string
  servername: string
  serverTotalStar: number
  serverAvatarUrl: string | null
}

export interface UploadUrlResponse {
  uploadUrl: string
  resourceId: string
}

export type StorageStatus = 'PENDING' | 'UPLOADED' | 'ACTIVE' | 'DELETED'

export interface ResourceStatusResponse {
  status: StorageStatus
}
