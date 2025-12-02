package com.example.demo.repository

import com.example.demo.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findByTaskIdOrderByCreatedAtAsc(taskId: Long): List<Comment>
}
