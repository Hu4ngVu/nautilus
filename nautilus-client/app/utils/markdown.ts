import type MarkdownIt from 'markdown-it'

let renderer: Promise<{ md: InstanceType<typeof MarkdownIt>, sanitize: (html: string) => string }> | null = null

// Loaded on demand so markdown-it/DOMPurify stay out of the main bundle
function load() {
  renderer ??= Promise.all([import('markdown-it'), import('dompurify')]).then(([{ default: MarkdownIt }, { default: DOMPurify }]) => {
    const md = new MarkdownIt({ html: false, linkify: true, breaks: true })
    DOMPurify.addHook('afterSanitizeAttributes', (node) => {
      if (node.tagName === 'A') {
        node.setAttribute('target', '_blank')
        node.setAttribute('rel', 'noopener noreferrer nofollow')
      }
    })
    return { md, sanitize: (html: string) => DOMPurify.sanitize(html) }
  })
  return renderer
}

export async function renderMarkdown(source: string): Promise<string> {
  const { md, sanitize } = await load()
  return sanitize(md.render(source))
}
