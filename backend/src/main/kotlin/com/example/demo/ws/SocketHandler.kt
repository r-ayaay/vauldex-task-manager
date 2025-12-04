package com.example.demo.ws

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArraySet

@Component
class SocketHandler : TextWebSocketHandler() {

    private val sessions = CopyOnWriteArraySet<WebSocketSession>()
    private val mapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions.add(session)
        println("New WebSocket connection: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        println("Received message: ${message.payload}")

        // Broadcast to all other clients
        for (s in sessions) {
            if (s.isOpen && s.id != session.id) {
                s.sendMessage(TextMessage(message.payload))
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
        println("WebSocket closed: ${session.id}")
    }

    // Optional helper to broadcast messages from server
    fun broadcast(event: Any) {
        val json = mapper.writeValueAsString(event)
        sessions.forEach {
            if (it.isOpen) it.sendMessage(TextMessage(json))
        }
    }
}
