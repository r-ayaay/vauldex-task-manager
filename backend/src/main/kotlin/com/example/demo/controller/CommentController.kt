package com.example.demo.controller

import com.example.demo.dto.CommentDTO
import com.example.demo.mapper.toDTO
import com.example.demo.repository.UserRepository
import com.example.demo.service.CommentService
import com.example.demo.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks/{taskId}/comments")
class CommentController(
    private val commentService: CommentService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    private fun getCurrentUser(request: HttpServletRequest) =
        request.getHeader("Authorization")?.removePrefix("Bearer ")?.let { token ->
            val sub = jwtUtil.extractUsername(token)
            userRepository.findByUsername(sub)
                ?: throw IllegalArgumentException("User not found")
        } ?: throw IllegalArgumentException("Missing Authorization header")

    @PostMapping
    fun addComment(
        @PathVariable taskId: Long,
        @RequestBody body: Map<String, String>,
        request: HttpServletRequest
    ): CommentDTO {
        val user = getCurrentUser(request)
        val content = body["content"] ?: throw IllegalArgumentException("Content required")
        return commentService.addComment(taskId, user.id, content).toDTO()
    }

    @GetMapping
    fun getComments(@PathVariable taskId: Long): List<CommentDTO> {
        return commentService.getCommentsForTask(taskId).map { it.toDTO() }
    }
}
