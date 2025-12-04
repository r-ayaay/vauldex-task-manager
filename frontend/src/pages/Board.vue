<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import TaskCard from '@/components/TaskCard.vue'
import AddCardBox from '@/components/AddCardBox.vue'
import TaskModal from '@/components/ui/modals/TaskModal.vue'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth'

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

const auth = useAuthStore()
const tasks = ref<Task[]>([])
const boardMembers = ref<User[]>([])
const route = useRoute()

// Reactive boardId
const boardId = computed(() => Number(route.params.id))

// Filtered tasks by status
const toDoTasks = computed(() => tasks.value.filter((t) => t.status === 'TO_DO'))
const inProgressTasks = computed(() => tasks.value.filter((t) => t.status === 'IN_PROGRESS'))
const completedTasks = computed(() => tasks.value.filter((t) => t.status === 'COMPLETED'))

// Fetch tasks for the current board
async function fetchTasks() {
  const id = boardId.value
  if (!id) return
  try {
    const res = await api.get(`/boards/${id}/tasks`)
    tasks.value = res.data.map((t: any) => ({
      id: t.id,
      content: t.content,
      assignedMember: t.assignedMemberUsername || 'Unassigned',
      status: t.status,
      creatorId: t.creatorId,
      assignedMemberId: t.assignedMemberId,
      boardOwnerId: t.boardOwnerId,
    }))
  } catch (err) {
    console.error(err)
  }
}

// Fetch board members
async function fetchBoardMembers() {
  const id = boardId.value
  if (!id) return
  try {
    const res = await api.get<User[]>(`/boards/${id}/members`)
    boardMembers.value = res.data
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
    if (
      latest?.type === 'TASK_CREATED' ||
      latest?.type === 'TASK_UPDATED' ||
      latest?.type === 'TASK_DELETED'
    ) {
      fetchTasks()
    }
  },
)

// Watch boardId to refetch tasks and members
watch(
  boardId,
  () => {
    fetchTasks()
    fetchBoardMembers()
  },
  { immediate: true },
)

// Determine if current user can edit task status
function canEdit(task: Task) {
  const userId = auth.user?.id
  return (
    userId === task.creatorId || userId === task.assignedMemberId || userId === task.boardOwnerId
  )
}

// Update task status
async function updateTaskStatus(taskId: number, newStatus: Task['status']) {
  try {
    await api.patch(`/tasks/${taskId}`, { status: newStatus })
    const task = tasks.value.find((t) => t.id === taskId)
    if (task) task.status = newStatus
  } catch (err) {
    console.error(err)
  }
}

// Adding task state
const addingStatus = ref<null | Task['status']>(null)
const newTaskContent = ref('')

function startAdding(status: Task['status']) {
  addingStatus.value = status
  newTaskContent.value = ''
}

function cancelAdding() {
  addingStatus.value = null
}

// Create a new task
async function createTask(status: Task['status']) {
  if (!newTaskContent.value.trim()) return
  const id = boardId.value
  if (!id) return

  try {
    const res = await api.post(`/tasks/board/${id}`, {
      content: newTaskContent.value,
      status,
    })
    tasks.value.push({
      id: res.data.id,
      content: res.data.content,
      assignedMember: res.data.assignedMember?.username || 'Unassigned',
      status: res.data.status,
      creatorId: res.data.creatorId,
      assignedMemberId: res.data.assignedMemberId,
      boardOwnerId: res.data.boardOwnerId,
    })
    addingStatus.value = null
    newTaskContent.value = ''
  } catch (err) {
    console.error(err)
  }
}

// Input focus for AddCardBox
const inputRef = ref<HTMLInputElement | null>(null)
watch(addingStatus, async (val) => {
  if (val !== null) (await nextTick(), inputRef.value?.focus())
})

// Task modal state
const showTaskModal = ref(false)
const selectedTask = ref<Task | null>(null)

// Open task modal
function openTaskModal(task: Task) {
  selectedTask.value = task
  showTaskModal.value = true
}

// Close task modal
function closeTaskModal() {
  showTaskModal.value = false
  selectedTask.value = null
}

function removeTask(taskId: number) {
  tasks.value = tasks.value.filter((t) => t.id !== taskId)

  // If the deleted task is currently open in the modal, close it
  if (selectedTask.value?.id === taskId) {
    closeTaskModal()
  }
}
</script>

<template>
  <div class="w-full h-full grid border-collapse grid-cols-3">
    <!-- TO_DO -->
    <div class="p-8 overflow-y-auto">
      <h2 class="text-xl font-semibold mb-4">To Do</h2>
      <div class="flex flex-col gap-4">
        <TaskCard
          v-for="task in toDoTasks"
          :key="task.id"
          :id="task.id"
          :content="task.content"
          :assignedMember="task.assignedMember"
          :status="task.status"
          :canEditStatus="canEdit(task)"
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus as Task['status'])"
          @click="openTaskModal(task)"
        />
        <AddCardBox
          :status="'TO_DO'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="cancelAdding"
        />
      </div>
    </div>

    <!-- IN_PROGRESS -->
    <div class="border-x border-[#4e5056] p-8 overflow-y-auto">
      <h2 class="text-xl font-semibold mb-4">In Progress</h2>
      <div class="flex flex-col gap-4">
        <TaskCard
          v-for="task in inProgressTasks"
          :key="task.id"
          :id="task.id"
          :content="task.content"
          :assignedMember="task.assignedMember"
          :status="task.status"
          :canEditStatus="canEdit(task)"
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus as Task['status'])"
          @click="openTaskModal(task)"
        />
        <AddCardBox
          :status="'IN_PROGRESS'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="cancelAdding"
        />
      </div>
    </div>

    <!-- COMPLETED -->
    <div class="p-8 overflow-y-auto">
      <h2 class="text-xl font-semibold mb-4">Completed</h2>
      <div class="flex flex-col gap-4">
        <TaskCard
          v-for="task in completedTasks"
          :key="task.id"
          :id="task.id"
          :content="task.content"
          :assignedMember="task.assignedMember"
          :status="task.status"
          :canEditStatus="canEdit(task)"
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus as Task['status'])"
          @click="openTaskModal(task)"
        />
        <AddCardBox
          :status="'COMPLETED'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="cancelAdding"
        />
      </div>
    </div>

    <!-- Task Modal -->
    <TaskModal
      v-if="showTaskModal && selectedTask"
      :task="selectedTask"
      :can-edit-status="selectedTask ? canEdit(selectedTask) : false"
      :board-members="boardMembers"
      @close="closeTaskModal"
      @delete="removeTask"
    />
  </div>
</template>
