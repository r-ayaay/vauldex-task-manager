package com.example.demo.controller


import com.example.demo.dto.RegisterRequest
import com.example.demo.dto.UserResponse
import com.example.demo.entity.User
import com.example.demo.repository.UserRepository
import com.example.demo.service.UserService
import com.example.demo.util.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<UserResponse> {
        val userResponse = userService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: RegisterRequest): ResponseEntity<Map<String, Any>> {
        val user = userRepository.findByUsername(request.username)
            ?: return ResponseEntity.status(401)
                .body(mapOf("error" to "Invalid credentials"))

        if (!passwordEncoder.matches(request.password, user.password)) {
            return ResponseEntity.status(401)
                .body(mapOf("error" to "Invalid credentials"))
        }

        val token = jwtUtil.generateToken(user.username)
        return ResponseEntity.ok(
            mapOf(
                "token" to token,
                "username" to user.username,
                "user_id" to user.id
            )
        )
    }
}

