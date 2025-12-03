package com.example.demo.controller

import com.example.demo.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    // Get users not in a specific board (available to add)
    @GetMapping("/available")
    fun getAvailableUsers(@RequestParam boardId: Long): List<Map<String, Any>> {
        return userService.getUsersNotInBoard(boardId).map { user ->
            mapOf(
                "id" to user.id,
                "username" to user.username
            )
        }
    }
}
