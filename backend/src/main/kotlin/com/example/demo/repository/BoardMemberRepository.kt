package com.example.demo.repository

import com.example.demo.entity.Board
import com.example.demo.entity.BoardMember
import org.springframework.data.jpa.repository.JpaRepository

interface BoardMemberRepository : JpaRepository<BoardMember, Long> {

    fun existsByBoardIdAndUserId(boardId: Long, userId: Long): Boolean

    fun findByBoardId(boardId: Long): List<BoardMember>

    fun deleteByBoardIdAndUserId(boardId: Long, userId: Long)

    fun findAllByBoard(board: Board): List<BoardMember>
}
