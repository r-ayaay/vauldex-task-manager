<template>
  <div class="px-8 py-16">
    <!-- Create Board Button -->
    <button
      @click="showModal = true"
      class="mb-10 px-6 py-3 bg-blue-600 hover:bg-blue-700 transition rounded-xl text-white font-semibold cursor-pointer"
    >
      + Create Board
    </button>

    <!-- Board Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      <!-- Board Cards -->
      <BoardCard
        v-for="(board, index) in boards"
        :key="board.id"
        :id="board.id"
        :name="board.name"
        :color="colors[index % colors.length]"
        @open="goToBoard"
      />
    </div>

    <!-- Modal -->
    <div
      v-if="showModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BoardCard from '@/components/BoardCard.vue'
import api from '@/api/axios' // your axios instance

type Board = {
  id: number
  name: string
}

const router = useRouter()

// UI State
const showModal = ref(false)
const newBoardName = ref('')

// Colors for cards
const colors = ['#EF4444', '#3B82F6', '#22C55E', '#EAB308', '#B320C5', '#FF5722', '#009688']

// Board list
const boards = ref<Board[]>([])

// Fetch on load
onMounted(fetchBoards)

async function fetchBoards() {
  try {
    const res = await api.get('/boards')
    boards.value = res.data
  } catch (err) {
    console.error(err)
  }
}

async function createBoard() {
  if (!newBoardName.value.trim()) return

  try {
    const res = await api.post('/boards', { name: newBoardName.value })
    boards.value.push(res.data)

    newBoardName.value = ''
    showModal.value = false
  } catch (err) {
    console.error(err)
  }
}

function goToBoard(id: number) {
  router.push(`/board/${id}`)
}
</script>
