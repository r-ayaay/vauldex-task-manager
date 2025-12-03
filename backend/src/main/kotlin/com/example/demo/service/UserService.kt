package com.example.demo.service

import com.example.demo.dto.RegisterRequest
import com.example.demo.dto.UserResponse
import com.example.demo.entity.User
import com.example.demo.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(@Valid @RequestBody request: RegisterRequest): UserResponse {
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("Username already taken")
        }

        val hashedPassword = passwordEncoder.encode(request.password)!!

        val user = userRepository.save(
            User(
                username = request.username,
                password = hashedPassword
            )
        )

        return UserResponse(user.id, user.username)
    }

    fun getUsersNotInBoard(boardId: Long): List<User> {
        return userRepository.findUsersNotInBoard(boardId)
    }
}
