package uz.sharif.vocabbrain.core.architecture

import androidx.compose.runtime.Stable

/**
 * Models an asynchronous read: not started, running, loaded, or failed.
 *
 * Replaces the `isLoading` + `data` + `errorMessage` trio in a UiState, which can
 * express impossible combinations (loading *and* failed) that the UI must then guard.
 * [Loading] and [Failure] carry the previous data so the screen can keep showing it
 * while refreshing or after a failed refresh.
 */
@Stable
sealed interface AsyncData<out T> {

    data object Uninitialized : AsyncData<Nothing>

    data class Loading<out T>(val prevData: T? = null) : AsyncData<T>

    data class Success<out T>(val data: T) : AsyncData<T>

    data class Failure<out T>(val error: Throwable, val prevData: T? = null) : AsyncData<T>

    /** Loaded data, or the last known data while loading or after a failure. May be stale. */
    fun dataOrNull(): T? = when (this) {
        is Success -> data
        is Loading -> prevData
        is Failure -> prevData
        Uninitialized -> null
    }

    fun errorOrNull(): Throwable? = (this as? Failure)?.error

    val isLoading: Boolean get() = this is Loading
}

inline fun <T, R> AsyncData<T>.map(transform: (T) -> R): AsyncData<R> = when (this) {
    is AsyncData.Success -> AsyncData.Success(transform(data))
    is AsyncData.Loading -> AsyncData.Loading(prevData?.let(transform))
    is AsyncData.Failure -> AsyncData.Failure(error, prevData?.let(transform))
    AsyncData.Uninitialized -> AsyncData.Uninitialized
}
