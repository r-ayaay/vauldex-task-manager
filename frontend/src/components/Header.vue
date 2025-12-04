<template>
  <header class="flex bg-[#1f1f21] items-center justify-between h-16 px-6 md:px-8 shadow-sm">
    <div class="flex items-center gap-2">
      <router-link to="/">
        <h1 class="text-2xl font-semibold text-white pr-8 cursor-pointer">TaskMan</h1>
      </router-link>

      <!-- Rename Board Button -->
      <div
        v-if="activeBoard && isBoardOwner"
        class="cursor-pointer stroke-[#4c5056] hover:stroke-white"
        @click="openRenameModal"
      >
        <PenIcon />
      </div>

      <!-- Board dropdown -->
      <div v-if="activeBoard" class="relative" ref="boardDropdownRef">
        <button
          @click="toggleBoardDropdown"
          class="flex items-center gap-1 text-lg pr-4 font-medium text-white hover:opacity-80 cursor-pointer"
        >
          {{ activeBoard.name }}
          <span><DownArrow /></span>
        </button>

        <div
          v-if="boardDropdownOpen"
          class="absolute left-0 mt-2 bg-white text-black rounded-md shadow-md w-48 z-50 overflow-hidden"
        >
          <div
            v-for="board in otherBoards"
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

      <!-- Members dropdown -->
      <div v-if="activeBoard" class="relative ml-4" ref="membersDropdownRef">
        <button
          @click="toggleMembersDropdown"
          class="flex items-center gap-1 text-lg font-medium text-white hover:opacity-80 cursor-pointer"
        >
          Members
          <span><DownArrow /></span>
        </button>

        <div
          v-if="membersDropdownOpen"
          class="absolute left-0 mt-2 bg-white text-black rounded-md shadow-md w-48 z-50 overflow-hidden"
        >
          <div
            v-if="isBoardOwner"
            @click="openAddMemberModal"
            class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-blue-600 font-medium"
          >
            + Add Member
          </div>

          <div
            v-for="member in boardMembers"
            :key="member.id"
            class="px-4 py-2 hover:bg-gray-100 cursor-pointer truncate"
          >
            {{ member.username }}
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
        class="absolute right-0 mt-2 bg-white border border-gray-200 rounded-lg shadow-md z-50 min-w-[160px] overflow-hidden"
      >
        <Button
          label="Delete Board"
          @click="showDeleteBoardModal = true"
          classNames="flex w-full items-center gap-2 px-4 py-2 text-red-600 hover:bg-red-100 transition-colors"
          v-if="activeBoard && isBoardOwner"
        />
        <Button
          label="Logout"
          @click="logoutHandler"
          classNames="flex w-full items-center gap-2 px-4 py-2 text-[#1C274C] hover:bg-[#f0f2f8] focus:bg-[#e3e7f3] transition-colors stroke-[#1C274C]"
        />
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

    <!-- Rename Board Modal -->
    <div
      v-if="showRenameModal && activeBoard"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-[#1B2028] p-8 rounded-xl w-[350px]">
        <h2 class="text-xl font-semibold mb-4">Rename Board</h2>
        <input
          v-model="renameBoardName"
          type="text"
          placeholder="New board name"
          class="w-full px-4 py-2 rounded-lg bg-gray-800 text-white mb-4 outline-none"
        />
        <div class="flex justify-end gap-4">
          <button
            @click="showRenameModal = false"
            class="px-4 py-2 rounded-lg bg-gray-600 hover:bg-gray-700 text-white"
          >
            Cancel
          </button>
          <button
            @click="renameBoard"
            class="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white"
          >
            Rename
          </button>
        </div>
      </div>
    </div>

    <!-- Add Member Modal -->
    <div
      v-if="showMembersModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-[#1B2028] p-6 rounded-xl w-[350px]">
        <h2 class="text-xl font-semibold mb-4">Add Member</h2>

        <select v-model="selectedUserId" class="w-full p-2 rounded-lg bg-gray-800 text-white mb-4">
          <option value="null" disabled>Select user to add</option>
          <option v-for="user in allUsers" :key="user.id" :value="user.id">
            {{ user.username }}
          </option>
        </select>

        <button
          @click="addMember"
          class="w-full px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white mb-2"
        >
          Add Member
        </button>

        <button
          @click="showMembersModal = false"
          class="w-full px-4 py-2 rounded-lg bg-gray-600 hover:bg-gray-700 text-white"
        >
          Cancel
        </button>
      </div>
    </div>

    <!-- Delete Board Modal -->
    <div
      v-if="showDeleteBoardModal && activeBoard"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    >
      <div class="bg-[#1B2028] p-8 rounded-xl w-[350px]">
        <h2 class="text-xl font-semibold mb-4">Delete Board</h2>
        <p class="mb-6">
          Are you sure you want to delete <strong>{{ activeBoard.name }}</strong
          >?
        </p>
        <div class="flex justify-end gap-4">
          <button
            @click="showDeleteBoardModal = false"
            class="px-4 py-2 rounded-lg bg-gray-600 hover:bg-gray-700 text-white"
          >
            Cancel
          </button>
          <button
            @click="deleteBoard"
            class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white"
          >
            Delete
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Button from './Button.vue'
import { useAuthStore } from '@/stores/auth'
import { useBoardStore } from '@/stores/boards'
import api from '@/api/axios'

import DownArrow from '@/components/ui/icons/down-arrow.vue'
import PenIcon from './ui/icons/pen-icon.vue'

interface Board {
  id: number
  name: string
  ownerId: number
}
interface User {
  id: number
  username: string
}

const auth = useAuthStore()
const boardStore = useBoardStore()
const router = useRouter()
const route = useRoute()
const props = defineProps<{ username: string; onLogout: () => void }>()

// Dropdowns
const boardDropdownOpen = ref(false)
const boardDropdownRef = ref<HTMLElement | null>(null)
const membersDropdownOpen = ref(false)
const membersDropdownRef = ref<HTMLElement | null>(null)
const userDropdownOpen = ref(false)
const userDropdownRef = ref<HTMLElement | null>(null)

// Modals
const showModal = ref(false)
const showRenameModal = ref(false)
const showMembersModal = ref(false)
const showDeleteBoardModal = ref(false)
const newBoardName = ref('')
const renameBoardName = ref('')

// Members
const boardMembers = ref<User[]>([])
const allUsers = ref<User[]>([])
const selectedUserId = ref<number | null>(null)

// Active board
const activeBoard = computed(() =>
  route.name === 'board' ? boardStore.getById(Number(route.params.id)) : null,
)
const isBoardOwner = computed(() => auth.user?.id === activeBoard.value?.ownerId)
const otherBoards = computed(() =>
  boardStore.boards.filter((b) => !activeBoard.value || b.id !== activeBoard.value.id),
)

// Functions
async function fetchBoardMembers() {
  if (!activeBoard.value) return
  try {
    const res = await api.get<User[]>(`/boards/${activeBoard.value.id}/members`)
    boardMembers.value = res.data
  } catch (err) {
    console.error(err)
  }
}
async function fetchAllUsers() {
  if (!activeBoard.value) return
  try {
    const res = await api.get<User[]>('/users/available', {
      params: { boardId: activeBoard.value.id },
    })
    allUsers.value = res.data
  } catch (err) {
    console.error(err)
  }
}
async function addMember() {
  if (!activeBoard.value || !selectedUserId.value) return
  try {
    await api.post(`/boards/${activeBoard.value.id}/members`, { memberId: selectedUserId.value })
    await fetchBoardMembers()
    selectedUserId.value = null
    showMembersModal.value = false
  } catch (err) {
    console.error(err)
  }
}
function openAddMemberModal() {
  showMembersModal.value = true
  membersDropdownOpen.value = false
}
function openRenameModal() {
  showRenameModal.value = true
}
function toggleBoardDropdown() {
  boardDropdownOpen.value = !boardDropdownOpen.value
}
function toggleMembersDropdown() {
  membersDropdownOpen.value = !membersDropdownOpen.value
}
function toggleUserDropdown() {
  userDropdownOpen.value = !userDropdownOpen.value
}
function selectBoard(id: number) {
  boardDropdownOpen.value = false
  router.push(`/board/${id}`)
}
function logoutHandler() {
  userDropdownOpen.value = false
  props.onLogout()
}
function handleClickOutside(e: MouseEvent) {
  if (boardDropdownRef.value && !boardDropdownRef.value.contains(e.target as Node))
    boardDropdownOpen.value = false
  if (membersDropdownRef.value && !membersDropdownRef.value.contains(e.target as Node))
    membersDropdownOpen.value = false
  if (userDropdownRef.value && !userDropdownRef.value.contains(e.target as Node))
    userDropdownOpen.value = false
}

// Lifecycle
onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
  boardStore.fetchBoards()
  fetchBoardMembers()
  fetchAllUsers()
})
onBeforeUnmount(() => document.removeEventListener('mousedown', handleClickOutside))
watch(activeBoard, (board) => {
  if (board) {
    fetchBoardMembers()
    fetchAllUsers()
    renameBoardName.value = board.name
  }
})

// CRUD
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
async function deleteBoard() {
  if (!activeBoard.value) return
  try {
    await api.delete(`/boards/${activeBoard.value.id}`)
    boardStore.boards = boardStore.boards.filter((b) => b.id !== activeBoard.value?.id)
    showDeleteBoardModal.value = false
    router.push('/')
  } catch (err) {
    console.error(err)
  }
}
async function renameBoard() {
  if (!activeBoard.value || !renameBoardName.value.trim()) return
  try {
    const res = await api.patch(`/boards/${activeBoard.value.id}`, { name: renameBoardName.value })
    const index = boardStore.boards.findIndex((b) => b.id === activeBoard.value?.id)
    if (index !== -1) boardStore.boards[index]!.name = res.data.name
    showRenameModal.value = false
  } catch (err) {
    console.error(err)
  }
}
</script>
