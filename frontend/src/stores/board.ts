import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useBoardStore = defineStore('board', {
  state: () => ({
    boards: [] as { id: number; name: string }[],
  }),

  actions: {
    async fetchBoards() {
      try {
        const res = await api.get('/boards')
        this.boards = res.data
      } catch (err) {
        console.error('Failed to fetch boards:', err)
      }
    },

    getById(id: number) {
      return this.boards.find((b) => b.id === id)
    },
  },
})
