package com.example.demo.repository

import com.example.demo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun existsByUsername(username: String): Boolean

    @Query("""
        SELECT u FROM User u
        WHERE u.id NOT IN (
            SELECT bm.user.id FROM BoardMember bm WHERE bm.board.id = :boardId
        )
    """)
    fun findUsersNotInBoard(@Param("boardId") boardId: Long): List<User>
}
