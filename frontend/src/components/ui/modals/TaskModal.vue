<template>
  <div
    class="modal-overlay fixed inset-0 bg-black/50 flex items-center justify-center z-50"
    @click="$emit('close')"
  >
    <div class="modal-content bg-[#1b2128] text-white rounded-xl w-1/2" @click.stop>
      <div class="px-8 pt-8 pb-2">
        <!-- Task Header -->
        <div class="flex justify-between">
          <h2 class="text-2xl font-semibold mb-8">{{ task.content }}</h2>
          <!-- Close button -->
          <button
            @click="$emit('close')"
            class="bg-red-600 hover:bg-red-700 text-white h-8 w-8 rounded cursor-pointer"
          >
            X
          </button>
        </div>

        <div class="flex gap-4">
          <!-- Status -->
          <div class="flex justify-between items-center gap-2">
            <label class="">Status:</label>
            <select
              v-model="localStatus"
              @change="updateStatus"
              class="bg-gray-700 text-white px-2 py-1 rounded cursor-pointer"
              :disabled="!canEditStatus"
            >
              <option value="TO_DO">To Do</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>

          <!-- Assigned Member -->
          <div class="flex justify-between items-center gap-2">
            <label class="">Assigned Member:</label>
            <select
              v-model="localAssignedMemberId"
              @change="updateAssignedMember"
              class="bg-gray-700 text-white px-2 py-1 rounded cursor-pointer"
              :disabled="!canAssignMember"
            >
              <option value="" disabled>Select member</option>
              <option v-for="member in boardMembers" :key="member.id" :value="member.id">
                {{ member.username }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <div class="px-8 py-2">
        <!-- Comments -->
        <div class="mb-4 border-t border-gray-600 pt-4">
          <h3 class="font-medium mb-2">Comments</h3>
          <div class="flex gap-2 mb-4">
            <input
              v-model="commentText"
              type="text"
              placeholder="Add a comment..."
              class="flex-1 px-2 py-1 rounded bg-gray-700 text-white outline-none"
              @keyup.enter.exact="addComment"
            />
            <button
              @click="addComment"
              class="px-3 py-1 bg-blue-600 hover:bg-blue-700 rounded text-white cursor-pointer"
            >
              Comment
            </button>
          </div>
          <ul class="max-h-80 overflow-y-auto mb-2">
            <li v-for="c in comments" :key="c.id" class="mb-4 bg-[#171a1b] p-2 rounded">
              <div>
                <strong>{{ c.username }} </strong>
                <span class="text-gray-400 text-sm font-light">
                  - {{ new Date(c.createdAt).toLocaleString() }}
                </span>
              </div>
              {{ c.content }}
            </li>
          </ul>
        </div>
      </div>
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
  createdAt: string
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
    comments.value = res.data.reverse()
  } catch (err) {
    console.error(err)
  }
}

import { useWebSocketStore } from '@/stores/ws'

const wsStore = useWebSocketStore()

watch(
  () => wsStore.events,
  (events) => {
    const latest = events[events.length - 1]
    if (latest?.type === 'COMMENT_CREATED') {
      fetchComments()
    }
  },
)

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
