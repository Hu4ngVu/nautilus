import type { ResourceStatusResponse, UploadUrlResponse } from '~/types/api'

export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
export const MAX_IMAGE_BYTES = 10 * 1024 * 1024

// Storage notifies the backend a few seconds after the object lands; only then can it be attached
const CONFIRM_TIMEOUT_MS = 30_000
const CONFIRM_INTERVAL_MS = 1_000

export class UploadValidationError extends Error {
  constructor(public reason: 'type' | 'size' | 'timeout') {
    super(reason)
  }
}

/** Uploads an image straight to object storage via a presigned URL and returns its resourceId. */
export function useUpload() {
  const api = useApi()
  const uploading = ref(false)

  async function waitUntilUploaded(resourceId: string) {
    const deadline = Date.now() + CONFIRM_TIMEOUT_MS
    while (Date.now() < deadline) {
      const { status } = await api<ResourceStatusResponse>(`/storage/resources/${encodeURIComponent(resourceId)}`)
      if (status === 'UPLOADED') return
      if (status !== 'PENDING') break
      await new Promise(resolve => setTimeout(resolve, CONFIRM_INTERVAL_MS))
    }
    throw new UploadValidationError('timeout')
  }

  async function upload(file: File): Promise<string> {
    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) throw new UploadValidationError('type')
    if (file.size <= 0 || file.size > MAX_IMAGE_BYTES) throw new UploadValidationError('size')

    uploading.value = true
    try {
      const { uploadUrl, resourceId } = await api<UploadUrlResponse>('/storage/upload-url', {
        query: { contentType: file.type, contentLength: file.size }
      })
      // Presigned URL expires after one minute and is signed with the content type
      const res = await fetch(uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },
        body: file
      })
      if (!res.ok) throw new Error(`Upload failed: ${res.status}`)
      await waitUntilUploaded(resourceId)
      return resourceId
    } finally {
      uploading.value = false
    }
  }

  return { upload, uploading }
}
