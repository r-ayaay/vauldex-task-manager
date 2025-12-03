<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import PlusIcon from './ui/icons/plus-icon.vue'

interface Props {
  status: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED'
  addingStatus: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED' | null
  modelValue: string // <-- rename to support v-model
  startAdding: (status: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED') => void
  createTask: (status: 'TO_DO' | 'IN_PROGRESS' | 'COMPLETED') => void
  cancelAdding: () => void
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue'])
const inputRef = ref<HTMLInputElement | null>(null)

watch(
  () => props.addingStatus,
  async (val) => {
    if (val === props.status) {
      await nextTick()
      inputRef.value?.focus()
    }
  },
)
</script>

<template>
  <div
    class="w-full bg-[#232529] p-4 rounded-xl shadow-lg border-gray-600 cursor-pointer hover:scale-103 transition-all"
  >
    <div
      v-if="props.addingStatus !== props.status"
      class=""
      @click="props.startAdding(props.status)"
    >
      <div class="flex gap-2"><PlusIcon /> Add a Card</div>
    </div>

    <div v-else class="flex flex-col gap-2">
      <input
        ref="inputRef"
        :value="props.modelValue"
        @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        @keyup.enter="props.createTask(props.status)"
        class="p-2 rounded bg-[#1a1c1f] border border-gray-600 outline-none"
        placeholder="Enter task..."
        autofocus
      />

      <div class="flex justify-end gap-2">
        <button @click="props.cancelAdding" class="text-gray-400 hover:text-white">Cancel</button>
        <button
          @click="props.createTask(props.status)"
          class="px-3 py-1 bg-blue-600 rounded hover:bg-blue-700"
        >
          Add
        </button>
      </div>
    </div>
  </div>
</template>
