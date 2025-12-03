<template>
  <div
    class="w-full bg-[#232529] p-4 rounded-xl hover:scale-103 hover:outline-solid transition-all duration-250 cursor-pointer shadow-lg"
  >
    <div class="flex justify-between items-center">
      <div>
        <p class="font-medium">{{ content }}</p>
        <p class="text-sm text-gray-400">Assigned to: {{ assignedMember }}</p>
      </div>

      <div v-if="canEditStatus">
        <select
          v-model="localStatus"
          @change="onUpdateStatus(localStatus)"
          class="bg-gray-700 text-white px-2 py-1 rounded cursor-pointer"
        >
          <option value="TO_DO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>
      <div v-else>
        <!-- Use the formatter here -->
        <span class="px-2 py-1 rounded bg-gray-700 text-white">{{
          formatStatus(localStatus)
        }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  id: number
  content: string
  assignedMember: string
  status: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED'
  canEditStatus: boolean
  onUpdateStatus: (newStatus: string) => void
}

const props = defineProps<Props>()
const localStatus = ref(props.status)

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    TO_DO: 'To Do',
    IN_PROGRESS: 'In Progress',
    COMPLETED: 'Completed',
  }
  return map[status] || status
}

// Keep localStatus in sync with prop changes
watch(
  () => props.status,
  (newVal) => {
    localStatus.value = newVal
  },
)
</script>
