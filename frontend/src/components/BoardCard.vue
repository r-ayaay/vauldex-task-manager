<script setup lang="ts">
const props = defineProps<{
  id: number
  name: string
  color?: string
}>()

const emit = defineEmits<{
  (e: 'open', id: number): void
}>()

function handleClick() {
  emit('open', props.id)
}
</script>

<template>
  <div
    @click="handleClick"
    class="rounded-xl bg-[#1B2028] w-full transform hover:scale-105 hover:-translate-y-2 transition-all duration-150 cursor-pointer shadow-lg"
  >
    <div :style="{ backgroundColor: props.color }" class="h-[100px] rounded-t-xl"></div>
    <div class="p-4 text-white text-lg font-semibold truncate">
      {{ props.name }}
    </div>
  </div>
</template>
