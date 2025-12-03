import { defineStore } from 'pinia'
import api from '@/api/axios'

interface Board {
  id: number
  name: string
  ownerId: number
}

export const useBoardStore = defineStore('board', {
  state: () => ({
    boards: [] as Board[],
  }),

  actions: {
    async fetchBoards() {
      try {
        const res = await api.get('/boards')
        this.boards = res.data // List<BoardDTO>
      } catch (err) {
        console.error('Failed to fetch boards:', err)
      }
    },

    getById(id: number) {
      return this.boards.find((b) => b.id === id)
    },
  },
})
