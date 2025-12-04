<script setup lang="ts">
import { onMounted } from 'vue'
import { useWebSocketStore } from '@/stores/ws'

const wsStore = useWebSocketStore()

onMounted(() => {
  wsStore.connect()
})
</script>

<template>
  <RouterView />
</template>

<style scoped></style>
