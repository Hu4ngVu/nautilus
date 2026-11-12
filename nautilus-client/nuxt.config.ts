// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@nuxt/eslint',
    '@nuxt/ui',
    '@nuxt/image',
    '@nuxtjs/i18n',
    '@pinia/nuxt'
  ],

  ssr: false,

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

  runtimeConfig: {
    public: {
      // Same-origin path proxied to nautilus-server; override with NUXT_PUBLIC_API_BASE
      apiBase: '/api'
    }
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
    // Static SPA has no IPX runtime; images come straight from object storage
    provider: 'none'
  }
})
