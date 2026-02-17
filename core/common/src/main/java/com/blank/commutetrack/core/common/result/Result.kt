package com.blank.commutetrack.core.common.result

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>
    data object Loading : AppResult<Nothing>
}
