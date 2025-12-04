package com.example.demo.service

import com.example.demo.entity.Board
import com.example.demo.entity.BoardMember
import com.example.demo.entity.Task
import com.example.demo.entity.User
import com.example.demo.repository.BoardMemberRepository
import com.example.demo.repository.BoardRepository
import com.example.demo.repository.TaskRepository
import com.example.demo.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import com.example.demo.ws.SocketHandler
import com.example.demo.ws.WebSocketEvent

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val boardMemberRepository: BoardMemberRepository,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val socketHandler: SocketHandler
) {

    fun createBoard(ownerId: Long, name: String): Board {
        val owner = userRepository.findById(ownerId)
            .orElseThrow { IllegalArgumentException("Owner not found") }
        val board = Board(name = name, owner = owner)

        val newBoard = boardRepository.save(board)

        addMember(newBoard.id,ownerId,ownerId)

        val event = WebSocketEvent(
            type = "BOARD_CREATED",
            payload = mapOf(
                "id" to newBoard.id,
                "name" to newBoard.name,
                "ownerId" to newBoard.owner.id
            )
        )

        socketHandler.broadcast(event)

        return newBoard
    }

    @Transactional
    fun updateBoardName(boardId: Long, userId: Long, newName: String): Board {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }

        if (board.owner.id != userId) throw IllegalAccessException("Only owner can rename board")
        board.name = newName

        val event = WebSocketEvent(
            type = "BOARD_UPDATED",
            payload = mapOf(
                "id" to board.id,
                "name" to board.name
            )
        )

        return board
    }

    @Transactional
    fun deleteBoard(boardId: Long, userId: Long) {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }
        if (board.owner.id != userId) throw IllegalAccessException("Only owner can delete board")
        boardRepository.delete(board)

        val event = WebSocketEvent(
            type = "BOARD_DELETED",
            payload = mapOf(
                "id" to board.id
            )
        )

        socketHandler.broadcast(event)
    }

    @Transactional
    fun addMember(boardId: Long, ownerId: Long, memberId: Long): BoardMember {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }
        if (board.owner.id != ownerId) throw IllegalAccessException("Only owner can add members")

        val user = userRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (boardMemberRepository.existsByBoardIdAndUserId(boardId, memberId)) {
            throw IllegalArgumentException("User is already a member")
        }

        val boardMember = BoardMember(board = board, user = user)
        return boardMemberRepository.save(boardMember)
    }

    @Transactional
    fun removeMember(boardId: Long, ownerId: Long, memberId: Long) {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }
        if (board.owner.id != ownerId) throw IllegalAccessException("Only owner can remove members")

        boardMemberRepository.deleteByBoardIdAndUserId(boardId, memberId)
    }

    fun getBoardsForUser(userId: Long): List<Board> {
        val ownedBoards = boardRepository.findByOwnerId(userId)
        val memberBoards = boardRepository.findByMembersUserId(userId)
        return (ownedBoards + memberBoards).distinct()
    }

    fun getTasksForBoard(boardId: Long): List<Task> {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }
        return taskRepository.findAllByBoardId(boardId)
    }

    fun getMembersForBoard(boardId: Long): List<User> {
        val board = boardRepository.findById(boardId)
            .orElseThrow { IllegalArgumentException("Board not found") }
        return boardMemberRepository.findAllByBoard(board).map { it.user }
    }


}
