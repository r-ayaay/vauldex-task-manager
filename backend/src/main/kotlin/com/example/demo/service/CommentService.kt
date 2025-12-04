package com.example.demo.service

import com.example.demo.entity.Comment
import com.example.demo.repository.BoardMemberRepository
import com.example.demo.repository.CommentRepository
import com.example.demo.repository.TaskRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.example.demo.ws.SocketHandler
import com.example.demo.ws.WebSocketEvent

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val boardMemberRepository: BoardMemberRepository,
    private val userRepository: UserRepository,
    private val socketHandler: SocketHandler
) {

    fun addComment(taskId: Long, userId: Long, content: String): Comment {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (!isUserMember(task.board.id, userId)) {
            throw IllegalAccessException("User not member of board")
        }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val comment = Comment(
            task = task,
            user = user,
            content = content
        )

        val savedComment = commentRepository.save(comment)

        // WebSocket event
        val event = WebSocketEvent(
            type = "COMMENT_CREATED",
            payload = mapOf(
                "id" to savedComment.id,
                "taskId" to task.id,
                "userId" to user.id,
                "content" to savedComment.content,
                "createdAt" to savedComment.createdAt.toString()
            )
        )
        socketHandler.broadcast(event)

        return savedComment
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
