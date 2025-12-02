package com.example.demo.repository

import com.example.demo.entity.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {

    fun findByBoardId(boardId: Long): List<Task>

    fun findByAssignedMemberId(userId: Long): List<Task>

    fun findByCreatorId(userId: Long): List<Task>
}
