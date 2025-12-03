package com.example.demo.dto

data class CommentDTO(
    val id: Long,
    val taskId: Long,
    val userId: Long,
    val username: String,
    val content: String,
    val createdAt: Long
)
