<template>
  <header class="flex bg-[#1f1f21] items-center justify-between h-16 px-6 md:px-8 shadow-sm">
    <div class="flex items-center gap-3">
      <h1 class="text-2xl font-semibold text-white pr-8 border-r">[Project Name]</h1>

      <!-- Board dropdown -->
      <div v-if="activeBoard" class="relative" ref="boardDropdownRef">
        <button
          @click="toggleBoardDropdown"
          class="flex items-center gap-1 text-lg px-4 font-medium text-white hover:opacity-80 cursor-pointer"
        >
          {{ activeBoard.name }}
          <span>▼</span>
        </button>

        <div
          v-if="boardDropdownOpen"
          class="absolute left-0 mt-2 bg-white text-black rounded-md shadow-md w-48 z-50 overflow-hidden"
        >
          <div
            v-for="board in boardStore.boards"
            :key="board.id"
            @click="selectBoard(board.id)"
            class="px-4 py-2 hover:bg-gray-100 cursor-pointer truncate"
          >
            {{ board.name }}
          </div>

          <hr class="my-1 border-gray-200" />

          <div
            @click="showModal = true"
            class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-blue-600 font-medium"
          >
            + Create Board
          </div>
        </div>
      </div>
    </div>

    <!-- User dropdown -->
    <div class="relative" ref="userDropdownRef">
      <Button
        :label="username"
        @click="toggleUserDropdown"
        classNames="flex items-center gap-2 px-3 py-1.5 text-white rounded-md hover:bg-white hover:text-[#1C274C] focus:ring-2 focus:ring-[#1C274C]/40 transition-all font-medium"
      ></Button>

      <div
        v-if="userDropdownOpen"
        class="absolute right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-md z-50 min-w-[120px] overflow-hidden"
      >
        <Button
          label="Logout"
          @click="logoutHandler"
          classNames="flex w-full items-center gap-2 px-4 py-2 text-[#1C274C] hover:bg-[#f0f2f8] focus:bg-[#e3e7f3] transition-colors stroke-[#1C274C]"
        ></Button>
      </div>
    </div>

    <!-- Create Board Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-[#1B2028] p-8 rounded-xl w-[350px]">
        <h2 class="text-xl font-semibold mb-4">Create New Board</h2>
        <input
          v-model="newBoardName"
          type="text"
          placeholder="Board name"
          class="w-full px-4 py-2 rounded-lg bg-gray-800 text-white mb-4 outline-none"
        />
        <div class="flex justify-end gap-4">
          <button
            @click="showModal = false"
            class="px-4 py-2 rounded-lg bg-gray-600 hover:bg-gray-700 text-white"
          >
            Cancel
          </button>
          <button
            @click="createBoard"
            class="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white"
          >
            Create
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from './Button.vue'
import { useAuthStore } from '@/stores/auth'
import { useBoardStore } from '@/stores/board'
import api from '@/api/axios'

const auth = useAuthStore()
const boardStore = useBoardStore()
const router = useRouter()
const route = useRoute()

const props = defineProps<{ username: string; onLogout: () => void }>()

// Dropdown state
const boardDropdownOpen = ref(false)
const boardDropdownRef = ref<HTMLElement | null>(null)
const userDropdownOpen = ref(false)
const userDropdownRef = ref<HTMLElement | null>(null)

// Create board modal state
const showModal = ref(false)
const newBoardName = ref('')

// Active board
const activeBoard = computed(() => {
  if (route.name !== 'board') return null
  const id = Number(route.params.id)
  return boardStore.getById(id)
})

// Toggle dropdowns
function toggleBoardDropdown() {
  boardDropdownOpen.value = !boardDropdownOpen.value
}
function toggleUserDropdown() {
  userDropdownOpen.value = !userDropdownOpen.value
}

// Select board
function selectBoard(id: number) {
  boardDropdownOpen.value = false
  router.push(`/board/${id}`)
}

// Logout
function logoutHandler() {
  userDropdownOpen.value = false
  props.onLogout()
}

// Handle outside click
function handleClickOutside(e: MouseEvent) {
  if (boardDropdownRef.value && !boardDropdownRef.value.contains(e.target as Node))
    boardDropdownOpen.value = false
  if (userDropdownRef.value && !userDropdownRef.value.contains(e.target as Node))
    userDropdownOpen.value = false
}
onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
  boardStore.fetchBoards()
})
onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})

// Create board
async function createBoard() {
  if (!newBoardName.value.trim()) return
  try {
    const res = await api.post('/boards', { name: newBoardName.value })
    boardStore.boards.push(res.data)
    newBoardName.value = ''
    showModal.value = false
  } catch (err) {
    console.error(err)
  }
}

// Open modal from dropdown
function goCreateBoard() {
  showModal.value = true
  boardDropdownOpen.value = false
}
</script>
