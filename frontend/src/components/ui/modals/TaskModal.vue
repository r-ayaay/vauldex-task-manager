<template>
  <div
    class="modal-overlay fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="modal-content bg-gray-800 text-white p-6 rounded-lg w-1/2">
      <!-- Task Header -->
      <h2 class="text-2xl font-semibold mb-4">Task Details</h2>
      <p class="mb-2"><strong>Content:</strong> {{ task.content }}</p>

      <!-- Assigned Member -->
      <div v-if="canAssignMember" class="mb-4">
        <label class="block mb-1">Assign Member:</label>
        <select
          v-model="localAssignedMemberId"
          @change="updateAssignedMember"
          class="w-full bg-gray-700 text-white px-2 py-1 rounded cursor-pointer"
        >
          <option value="" disabled>Select member</option>
          <option v-for="member in boardMembers" :key="member.id" :value="member.id">
            {{ member.username }}
          </option>
        </select>
      </div>

      <!-- Status -->
      <div class="mb-4">
        <label class="block mb-1">Status:</label>
        <select
          v-model="localStatus"
          @change="updateStatus"
          class="w-full bg-gray-700 text-white px-2 py-1 rounded cursor-pointer"
          :disabled="!canEditStatus"
        >
          <option value="TO_DO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>

      <!-- Comments -->
      <div class="mb-4">
        <h3 class="font-medium mb-2">Comments</h3>
        <ul class="max-h-48 overflow-y-auto mb-2">
          <li v-for="c in comments" :key="c.id" class="mb-1 border-b border-gray-600 pb-1">
            <strong>{{ c.username }}:</strong> {{ c.content }}
          </li>
        </ul>
        <div class="flex gap-2">
          <input
            v-model="commentText"
            type="text"
            placeholder="Add a comment..."
            class="flex-1 px-2 py-1 rounded bg-gray-700 text-white outline-none"
          />
          <button
            @click="addComment"
            class="px-3 py-1 bg-blue-600 hover:bg-blue-700 rounded text-white"
          >
            Comment
          </button>
        </div>
      </div>

      <!-- Close button -->
      <button
        @click="$emit('close')"
        class="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded w-full"
      >
        Close
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/api/axios'

interface Task {
  id: number
  content: string
  assignedMember: string
  status: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED'
  creatorId: number
  assignedMemberId: number | null
  boardOwnerId: number
}

interface User {
  id: number
  username: string
}

interface CommentDTO {
  id: number
  content: string
  username: string
}

const props = defineProps<{ task: Task; canEditStatus: boolean; boardMembers: User[] }>()
const emit = defineEmits({
  close: null,
})

const auth = useAuthStore()

// Local state
const localAssignedMemberId = ref<number | null>(props.task.assignedMemberId)
const localStatus = ref(props.task.status)
const commentText = ref('')
const comments = ref<CommentDTO[]>([])

// Permissions
const canAssignMember = computed(
  () => auth.user?.id === props.task.creatorId || auth.user?.id === props.task.boardOwnerId,
)

// Format status
const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    TO_DO: 'To Do',
    IN_PROGRESS: 'In Progress',
    COMPLETED: 'Completed',
  }
  return map[status] || status
}

// Fetch comments
const fetchComments = async () => {
  try {
    const res = await api.get<CommentDTO[]>(`/tasks/${props.task.id}/comments`)
    comments.value = res.data
  } catch (err) {
    console.error(err)
  }
}

// Assign member
// Replace assignMember function with this
const updateAssignedMember = async () => {
  if (localAssignedMemberId.value === null) return
  try {
    await api.patch(`/tasks/${props.task.id}`, {
      assignedMember: localAssignedMemberId.value.toString(),
    })
    // Optionally, update props.task.assignedMemberId so it stays in sync
    props.task.assignedMemberId = localAssignedMemberId.value
  } catch (err) {
    console.error(err)
  }
}

// Update status
const updateStatus = async () => {
  try {
    await api.patch(`/tasks/${props.task.id}`, { status: localStatus.value })
    // Optionally show success feedback
  } catch (err) {
    console.error(err)
  }
}

// Add comment
const addComment = async () => {
  if (!commentText.value.trim()) return
  try {
    await api.post(`/tasks/${props.task.id}/comments`, { content: commentText.value })
    commentText.value = ''
    await fetchComments()
  } catch (err) {
    console.error(err)
  }
}

// Fetch initial comments
onMounted(() => {
  fetchComments()
})

// Sync with prop changes
watch(
  () => props.task.status,
  (newStatus) => {
    localStatus.value = newStatus
  },
)
watch(
  () => props.task.assignedMemberId,
  (newId) => {
    localAssignedMemberId.value = newId
  },
)
</script>

<style scoped>
.modal-overlay {
  z-index: 1000;
}
.modal-content select:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
