package com.example.demo.service

import com.example.demo.entity.Board
import com.example.demo.entity.Task
import com.example.demo.entity.TaskStatus
import com.example.demo.repository.BoardMemberRepository
import com.example.demo.repository.BoardRepository
import com.example.demo.repository.TaskRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val boardMemberRepository: BoardMemberRepository
) {

    fun createTask(
        boardId: Long,
        creatorId: Long,
        content: String,
        assignedMemberId: Long? = null,
        statusString: String
    ): Task {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }

        if (!isUserMember(board, creatorId)) throw IllegalAccessException("User not member of board")

        val creator = userRepository.findById(creatorId)
            .orElseThrow { IllegalArgumentException("Creator not found") }

        val assignedMember = assignedMemberId?.let { id ->
            if (!isUserMember(board, id)) throw IllegalArgumentException("Assigned member not in board")
            userRepository.findById(id).orElseThrow { IllegalArgumentException("User not found") }
        }


        val task = Task(
            content = content,
            assignedMember = assignedMember,
            board = board,
            creator = creator,
            status = statusString
        )

        return taskRepository.save(task)
    }


    @Transactional
    fun updateTaskContent(taskId: Long, userId: Long, newContent: String): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.creator.id != userId && task.board.owner.id != userId) {
            throw IllegalAccessException("Only creator or board owner can update task content")
        }

        task.content = newContent
        return task
    }

    @Transactional
    fun updateTaskAssignedMember(taskId: Long, assignedMemberId: Long): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        val user = userRepository.findById(assignedMemberId)
            .orElseThrow { IllegalArgumentException("User not found") }

        task.assignedMember = user

        return task
    }


    @Transactional
    fun updateTaskStatus(taskId: Long, userId: Long, status: String): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.assignedMember?.id != userId && task.creator.id != userId && task.board.owner.id != userId) {
            throw IllegalAccessException("Not allowed to update task status")
        }

        task.status = status
        return task
    }

    @Transactional
    fun deleteTask(taskId: Long, userId: Long) {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Task not found") }

        if (task.creator.id != userId && task.board.owner.id != userId) {
            throw IllegalAccessException("Only creator or board owner can delete task")
        }

        taskRepository.delete(task)
    }

    fun getTasksForBoard(boardId: Long): List<Task> {
        return taskRepository.findByBoardId(boardId)
    }

    private fun isUserMember(board: Board, userId: Long): Boolean {
        if (board.owner.id == userId) return true
        return boardMemberRepository.existsByBoardIdAndUserId(board.id, userId)
    }
}
