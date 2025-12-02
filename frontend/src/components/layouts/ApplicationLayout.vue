<template>
  <div class="flex h-screen overflow-hidden bg-[#31353F] text-white">
    <div class="flex flex-col flex-1 min-w-0">
      <Header :username="username" @logout="logout"></Header>
      <main class="flex-1 overflow-y-auto">
        <RouterView />
      </main>
    </div>
  </div>
  <div class="lg:hidden fixed bottom-0 left-0 w-full z-50 border-t border-gray-200"></div>
</template>

<script setup lang="ts">
import Header from '../Header.vue'
import { computed } from 'vue'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()

const username = computed(() => auth.user?.username || '')
function logout() {
  auth.logout()
  window.location.href = '/login'
}
</script>
