package com.pani.app.util.ext

/**
 * Lightweight result wrapper used across domain → presentation boundaries.
 *
 * Rules:
 *   - Repositories return raw Flow<T> (Room is the single source of truth).
 *   - Use cases wrap those flows in PaniResult so ViewModels get a typed
 *     loading / success / error signal without touching coroutine internals.
 */
sealed class PaniResult<out T> {
    data object Loading : PaniResult<Nothing>()
    data class Success<out T>(val data: T) : PaniResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : PaniResult<Nothing>()
}

val <T> PaniResult<T>.dataOrNull: T?
    get() = (this as? PaniResult.Success)?.data

val PaniResult<*>.isLoading: Boolean
    get() = this is PaniResult.Loading
