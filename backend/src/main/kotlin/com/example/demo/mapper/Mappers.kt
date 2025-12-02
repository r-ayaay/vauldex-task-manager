package com.example.demo.mapper

import com.example.demo.dto.BoardDTO
import com.example.demo.dto.CommentDTO
import com.example.demo.dto.TaskDTO
import com.example.demo.entity.Board
import com.example.demo.entity.Comment
import com.example.demo.entity.Task


fun Board.toDTO(): BoardDTO = BoardDTO(
    id = this.id,
    name = this.name,
    ownerId = this.owner.id
)

fun Task.toDTO(): TaskDTO = TaskDTO(
    id = this.id,
    content = this.content,
    status = this.status.name,
    assignedMemberId = this.assignedMember?.id,
    creatorId = this.creator.id
)

fun Comment.toDTO(): CommentDTO = CommentDTO(
    id = this.id,
    taskId = this.task.id,
    userId = this.user.id,
    content = this.content,
    createdAt = this.createdAt
)
