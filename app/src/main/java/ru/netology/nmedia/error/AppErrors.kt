package ru.netology.nmedia.error

sealed class AppErrors(open val msg: String): RuntimeException()

data class ApiError(override val msg: String): AppErrors(msg)
data class NetworkException(override val msg: String): AppErrors(msg)
data class UnknownException(override val msg: String): AppErrors(msg)