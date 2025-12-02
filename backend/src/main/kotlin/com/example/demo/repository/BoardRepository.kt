package com.example.demo.repository

import com.example.demo.entity.Board
import org.springframework.data.jpa.repository.JpaRepository

interface BoardRepository : JpaRepository<Board, Long> {
    fun findByOwnerId(ownerId: Long): List<Board>

    // Find boards where user is owner OR member
    fun findByMembersUserId(userId: Long): List<Board>
}
