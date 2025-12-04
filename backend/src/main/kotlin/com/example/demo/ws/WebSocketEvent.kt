package com.example.demo.ws

data class WebSocketEvent(
    val type: String,
    val payload: Map<String, Any>
)