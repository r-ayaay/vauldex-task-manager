package com.example.demo.dto

data class TaskDTO(
    val id: Long,
    val content: String,
    val status: String,
    val creatorId: Long,
    val assignedMemberId: Long?,
    val assignedMemberUsername: String?,
    val boardId: Long,
    val boardOwnerId: Long // <-- new field
)

