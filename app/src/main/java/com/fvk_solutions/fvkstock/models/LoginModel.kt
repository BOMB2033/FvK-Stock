package com.fvk_solutions.fvkstock.models
data class UserApiResponse(
    val userId: String,
    val displayName: String
)
data class LoginApiResponse(
    val token: String,
    val user: UserApiResponse
)
data class LoginRequest(
    val username: String,
    val password: String
)