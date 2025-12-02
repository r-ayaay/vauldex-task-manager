package com.example.demo.service

import com.example.demo.entity.Comment
import com.example.demo.repository.BoardMemberRepository
import com.example.demo.repository.CommentRepository
import com.example.demo.repository.TaskRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val boardMemberRepository: BoardMemberRepository,
    private val userRepository: UserRepository
) {

    fun addComment(taskId: Long, userId: Long, content: String): Comment {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (!isUserMember(task.board.id, userId)) {
            throw IllegalAccessException("User not member of board")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") } // unwrap Optional

        val comment = Comment(
            task = task,
            user = user,  // now it's a User object, not Optional<User>
            content = content
        )

        return commentRepository.save(comment)
    }


    fun getCommentsForTask(taskId: Long): List<Comment> {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
    }

    private fun isUserMember(boardId: Long, userId: Long): Boolean {
        // Owner is considered a member
        // Fetch board owner
        // Simplified check; inject BoardRepository if needed
        return boardMemberRepository.existsByBoardIdAndUserId(boardId, userId)
    }
}
