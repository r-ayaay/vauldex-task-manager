// stores/ws.ts
import { defineStore } from 'pinia'

export const useWebSocketStore = defineStore('ws', {
  state: () => ({
    socket: null as WebSocket | null,
    events: [] as any[],
  }),
  actions: {
    connect() {
      if (this.socket) return // already connected
      this.socket = new WebSocket('ws://localhost:8080/ws') // your endpoint

      this.socket.onopen = () => {
        console.log('WebSocket connected')
      }

      this.socket.onmessage = (event) => {
        const data = JSON.parse(event.data)
        console.log('Received WS event:', data)
        this.events = [...this.events, data]
      }

      this.socket.onclose = () => {
        console.log('WebSocket disconnected, retrying...')
        setTimeout(() => this.connect(), 3000) // simple reconnection
      }
    },
    send(event: any) {
      if (this.socket?.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify(event))
      }
    },
  },
})
