package gosex.backend.util

sealed class Result<out T, out E> {
  data class Success<out T>(val value: T) : Result<T, Nothing>()

  data class Error<out E>(val error: E, val message: String? = null) : Result<Nothing, E>()

  fun isSuccess(): Boolean = this is Success

  fun isError(): Boolean = this is Error

  fun getOrNull(): T? =
    when (this) {
      is Success -> value
      is Error -> null
    }

  fun errorOrNull(): E? =
    when (this) {
      is Success -> null
      is Error -> error
    }
}
