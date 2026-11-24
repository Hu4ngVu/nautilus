<script setup lang="ts">
import type { VersionMap } from '~/types/api'

const props = defineProps<{
  username: string
  servername: string
}>()

const { t } = useI18n()
const servers = useServerApi()
const { notify, success } = useApiError()

const java = reactive<{ versions: VersionMap, primary: string | null }>({ versions: {}, primary: null })
const bedrock = reactive<{ versions: VersionMap, primary: string | null }>({ versions: {}, primary: null })
const saving = ref(false)

const { status } = useAsyncData(`versions:${props.username}/${props.servername}`, async () => {
  const [j, b] = await Promise.all([
    servers.javaVersions(props.username, props.servername),
    servers.bedrockVersions(props.username, props.servername)
  ])
  java.versions = j.javaVersions ?? {}
  java.primary = j.primaryJavaVersion ?? null
  bedrock.versions = b.bedrockVersions ?? {}
  bedrock.primary = b.primaryBedrockVersion ?? null
  return true
})

async function save() {
  saving.value = true
  try {
    await Promise.all([
      servers.setJavaVersions(props.username, props.servername, {
        javaVersions: java.versions,
        primaryJavaVersion: java.primary
      }),
      servers.setBedrockVersions(props.username, props.servername, {
        bedrockVersions: bedrock.versions,
        primaryBedrockVersion: bedrock.primary
      })
    ])
    success(t('manage.saved'))
  } catch (error) {
    notify(error)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <USkeleton
      v-if="status === 'pending'"
      class="h-32"
    />
    <template v-else>
      <VersionEditor
        v-model:versions="java.versions"
        v-model:primary="java.primary"
        label="Java"
      />
      <VersionEditor
        v-model:versions="bedrock.versions"
        v-model:primary="bedrock.primary"
        label="Bedrock"
      />
      <UButton
        :label="t('common.save')"
        :loading="saving"
        class="self-start"
        @click="save"
      />
    </template>
  </div>
</template>
