package com.example.demo.controller

import com.example.demo.dto.BoardDTO
import com.example.demo.dto.TaskDTO
import com.example.demo.mapper.toDTO
import com.example.demo.repository.UserRepository
import com.example.demo.service.BoardService
import com.example.demo.service.TaskService
import com.example.demo.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/boards")
class BoardController(
    private val boardService: BoardService,
    private val taskService: TaskService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    // Helper to get User from JWT "sub"
    private fun getCurrentUser(request: HttpServletRequest) =
        request.getHeader("Authorization")?.removePrefix("Bearer ")?.let { token ->
            val sub = jwtUtil.extractUsername(token) // you need a JWT util to decode "sub"
            userRepository.findByUsername(sub)
                ?: throw IllegalArgumentException("User not found")
        } ?: throw IllegalArgumentException("Missing Authorization header")

    @PostMapping
    fun createBoard(@RequestBody body: Map<String, String>, request: HttpServletRequest): BoardDTO {
        val user = getCurrentUser(request)
        val name = body["name"] ?: throw IllegalArgumentException("Board name required")
        return boardService.createBoard(user.id, name).toDTO()
    }

    @PatchMapping("/{boardId}")
    fun updateBoardName(
        @PathVariable boardId: Long,
        @RequestBody body: Map<String, String>,
        request: HttpServletRequest
    ): BoardDTO {
        val user = getCurrentUser(request)
        val newName = body["name"] ?: throw IllegalArgumentException("New name required")
        return boardService.updateBoardName(boardId, user.id, newName).toDTO()
    }

    @DeleteMapping("/{boardId}")
    fun deleteBoard(@PathVariable boardId: Long, request: HttpServletRequest) {
        val user = getCurrentUser(request)
        boardService.deleteBoard(boardId, user.id)
    }

    @PostMapping("/{boardId}/members")
    fun addMember(
        @PathVariable boardId: Long,
        @RequestBody body: Map<String, Long>,
        request: HttpServletRequest
    ) {
        val user = getCurrentUser(request)
        val memberId = body["memberId"] ?: throw IllegalArgumentException("memberId required")
        boardService.addMember(boardId, user.id, memberId)
    }

    @DeleteMapping("/{boardId}/members/{memberId}")
    fun removeMember(@PathVariable boardId: Long, @PathVariable memberId: Long, request: HttpServletRequest) {
        val user = getCurrentUser(request)
        boardService.removeMember(boardId, user.id, memberId)
    }

    @GetMapping
    fun getBoards(request: HttpServletRequest): List<BoardDTO> {
        val user = getCurrentUser(request)
        return boardService.getBoardsForUser(user.id).map { it.toDTO() }
    }

    @GetMapping("/{boardId}/tasks")
    fun getTasksForBoard(@PathVariable boardId: Long, request: HttpServletRequest): List<TaskDTO> {
        val user = getCurrentUser(request)

        return boardService.getTasksForBoard(boardId).map { it.toDTO() }
    }

    @GetMapping("/{boardId}/members")
    fun getBoardMembers(@PathVariable boardId: Long, request: HttpServletRequest): List<Map<String, String>> {

        val user = getCurrentUser(request)
        val members = boardService.getMembersForBoard(boardId)
        // Return only username
        return members.map { mapOf("username" to it.username) }
    }

}
