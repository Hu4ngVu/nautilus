import { vRise } from '~/directives/rise'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('rise', vRise)
})
