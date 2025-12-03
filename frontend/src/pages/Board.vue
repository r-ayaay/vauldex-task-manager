<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import TaskCard from '@/components/TaskCard.vue'
import AddCardBox from '@/components/AddCardBox.vue'
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

const auth = useAuthStore()
const tasks = ref<Task[]>([])
const route = useRoute()

// Reactive boardId
const boardId = computed(() => Number(route.params.id))

// Computed filtered tasks
const toDoTasks = computed(() => tasks.value.filter((t) => t.status === 'TO_DO'))
const inProgressTasks = computed(() => tasks.value.filter((t) => t.status === 'IN_PROGRESS'))
const completedTasks = computed(() => tasks.value.filter((t) => t.status === 'COMPLETED'))

// Fetch tasks for the current board
async function fetchTasks() {
  const id = boardId.value
  if (!id) return // prevent undefined
  try {
    const res = await api.get(`/boards/${id}/tasks`)
    tasks.value = res.data.map((t: any) => ({
      id: t.id,
      content: t.content,
      assignedMember: t.assignedMember?.username || 'Unassigned',
      status: t.status,
      creatorId: t.creatorId,
      assignedMemberId: t.assignedMemberId,
      boardOwnerId: t.boardOwnerId,
    }))
  } catch (err) {
    console.error(err)
  }
}

// Watch boardId and refetch tasks whenever it changes
watch(boardId, fetchTasks, { immediate: true })

// Determine if current user can edit task status
function canEdit(task: Task) {
  const userId = auth.user?.id
  return (
    userId === task.creatorId || userId === task.assignedMemberId || userId === task.boardOwnerId
  )
}

// Update task status
async function updateTaskStatus(taskId: number, newStatus: string) {
  try {
    await api.patch(`/tasks/${taskId}`, { status: newStatus })
    const task = tasks.value.find((t) => t.id === taskId)
    if (task) task.status = newStatus as Task['status']
  } catch (err) {
    console.error(err)
  }
}

// State for adding tasks
const addingStatus = ref<null | 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED'>(null)
const newTaskContent = ref('')

function startAdding(status: Task['status']) {
  addingStatus.value = status
  newTaskContent.value = ''
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
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus)"
        />
        <AddCardBox
          :status="'TO_DO'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="() => (addingStatus = null)"
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
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus)"
        />
        <AddCardBox
          :status="'IN_PROGRESS'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="() => (addingStatus = null)"
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
          :onUpdateStatus="(newStatus) => updateTaskStatus(task.id, newStatus)"
        />
        <AddCardBox
          :status="'COMPLETED'"
          :addingStatus="addingStatus"
          v-model="newTaskContent"
          :startAdding="startAdding"
          :createTask="createTask"
          :cancelAdding="() => (addingStatus = null)"
        />
      </div>
    </div>
  </div>
</template>
