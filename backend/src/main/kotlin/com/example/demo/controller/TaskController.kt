package com.example.demo.controller

import com.example.demo.dto.TaskDTO
import com.example.demo.entity.TaskStatus
import com.example.demo.mapper.toDTO
import com.example.demo.repository.UserRepository
import com.example.demo.service.TaskService
import com.example.demo.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    private fun getCurrentUser(request: HttpServletRequest) =
        request.getHeader("Authorization")?.removePrefix("Bearer ")?.let { token ->
            val sub = jwtUtil.extractUsername(token)
            userRepository.findByUsername(sub)
                ?: throw IllegalArgumentException("User not found")
        } ?: throw IllegalArgumentException("Missing Authorization header")

    @PostMapping("/board/{boardId}")
    fun createTask(
        @PathVariable boardId: Long,
        @RequestBody body: Map<String, String>,
        request: HttpServletRequest
    ): TaskDTO {
        val user = getCurrentUser(request)
        val content = body["content"] ?: throw IllegalArgumentException("Task content required")
        val statusString = body["status"] ?: throw IllegalArgumentException("Task content required")
        val assignedMemberId = body["assignedMemberId"]?.toLong()

        return taskService.createTask(boardId, user.id, content, assignedMemberId, statusString).toDTO()
    }


    @PatchMapping("/{taskId}")
    fun updateTask(
        @PathVariable taskId: Long,
        @RequestBody body: Map<String, String>,
        request: HttpServletRequest
    ): TaskDTO {
        val user = getCurrentUser(request)
        val newContent = body["content"]
        val status = body["status"]
        val assignedMember = body["assignedMember"]

        val task = when {
            newContent != null -> taskService.updateTaskContent(taskId, user.id, newContent)
            status != null -> taskService.updateTaskStatus(taskId, user.id, status)
            assignedMember != null -> {
                val assignedMemberId = assignedMember.toLongOrNull()
                    ?: throw IllegalArgumentException("Invalid assignedMember id")
                taskService.updateTaskAssignedMember(taskId, assignedMemberId)
            }
            else -> throw IllegalArgumentException("Nothing to update")
        }
        return task.toDTO()
    }


    @DeleteMapping("/{taskId}")
    fun deleteTask(@PathVariable taskId: Long, request: HttpServletRequest) {
        val user = getCurrentUser(request)
        taskService.deleteTask(taskId, user.id)
    }
}
