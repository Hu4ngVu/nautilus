// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/fonts',
    '@nuxt/ui',
    '@nuxt/image',
    '@nuxtjs/i18n',
    '@pinia/nuxt'
  ],

  $development: {
    runtimeConfig: {
      // Server-side fetches skip the dev proxy and call the backend directly
      apiBaseServer: 'http://localhost:3279'
    }
  },

  devtools: {
    enabled: true
  },

  app: {
    head: {
      htmlAttrs: { lang: 'vi' },
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' }
      ],
      link: [
        { rel: 'icon', href: '/favicon.ico' }
      ]
    }
  },

  css: ['~/assets/css/main.css'],

  // localStorage access throws when site data is blocked and color-mode does not guard it
  colorMode: {
    storage: 'cookie'
  },

  runtimeConfig: {
    // Used while server-rendering, e.g. a private address of nautilus-server; override with NUXT_API_BASE_SERVER.
    // Empty falls back to apiBase.
    apiBaseServer: '',
    public: {
      // Browser-facing nautilus-server URL, e.g. https://api.example.com; override with NUXT_PUBLIC_API_BASE.
      // The default is the dev proxy below.
      apiBase: '/api'
    }
  },

  // Public pages are server-rendered for SEO and link previews. Pages behind login render in the
  // browser only: the session lives in a cookie on the API host, so the server always renders as a guest.
  routeRules: {
    '/login': { ssr: false },
    '/register': { ssr: false },
    '/auth/**': { ssr: false },
    '/dashboard': { ssr: false },
    '/stars': { ssr: false },
    '/settings': { ssr: false },
    '/new': { ssr: false },
    '/*/*/manage': { ssr: false },
    '/': { isr: { expiration: 5 * 60 } },
    '/browse': { isr: { expiration: 5 * 60 } }
  },

  devServer: {
    port: 8082
  },

  compatibilityDate: '2026-06-30',

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:3279',
        changeOrigin: true
      },
      // Spring Security OAuth2 endpoints live outside /api
      '/oauth2': {
        target: 'http://localhost:3279/oauth2',
        changeOrigin: false
      },
      '/login/oauth2': {
        target: 'http://localhost:3279/login/oauth2',
        changeOrigin: false
      }
    }
  },

  eslint: {
    config: {
      stylistic: {
        commaDangle: 'never',
        braceStyle: '1tbs'
      }
    }
  },

  // Self-hosted at build time; only the subsets the UI renders (Vietnamese diacritics included)
  fonts: {
    defaults: {
      subsets: ['latin', 'vietnamese'],
      styles: ['normal'],
      preload: false
    },
    families: [
      { name: 'Be Vietnam Pro', provider: 'google', weights: [400, 500, 600], preload: true },
      // Only referenced through --font-pixel, which the font scanner does not pick up
      { name: 'VT323', provider: 'google', weights: [400], global: true }
    ]
  },

  i18n: {
    strategy: 'no_prefix',
    defaultLocale: 'vi',
    locales: [
      { code: 'vi', language: 'vi-VN', name: 'Tiếng Việt', file: 'vi.json' },
      { code: 'en', language: 'en-US', name: 'English', file: 'en.json' }
    ],
    detectBrowserLanguage: {
      useCookie: true,
      cookieKey: 'i18n_redirected',
      redirectOn: 'root'
    }
  },

  image: {
    // Images come straight from object storage; no IPX resizing
    provider: 'none'
  }
})
