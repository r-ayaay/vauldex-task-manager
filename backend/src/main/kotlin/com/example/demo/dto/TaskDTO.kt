package com.example.demo.dto

data class TaskDTO(
    val id: Long,
    val content: String,
    val status: String,
    val assignedMemberId: Long?,
    val creatorId: Long
)

