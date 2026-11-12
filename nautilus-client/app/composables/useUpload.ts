import type { UploadUrlResponse } from '~/types/api'

export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
export const MAX_IMAGE_BYTES = 10 * 1024 * 1024

export class UploadValidationError extends Error {
  constructor(public reason: 'type' | 'size') {
    super(reason)
  }
}

/** Uploads an image straight to object storage via a presigned URL and returns its resourceId. */
export function useUpload() {
  const api = useApi()
  const uploading = ref(false)

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
      return resourceId
    } finally {
      uploading.value = false
    }
  }

  return { upload, uploading }
}
