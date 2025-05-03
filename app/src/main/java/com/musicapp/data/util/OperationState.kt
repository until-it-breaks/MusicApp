package com.musicapp.data.util

/**
 * General purpose states for async operations.
 */
sealed class OperationState {
    object Idle: OperationState()
    object Ongoing: OperationState()
    object Success: OperationState()
    data class Error(val message: String = "An error has occurred", val stringKey: Int? = null) : OperationState()
}
