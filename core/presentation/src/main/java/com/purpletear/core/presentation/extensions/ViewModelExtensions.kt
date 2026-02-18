package com.purpletear.core.presentation.extensions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
 

fun <T> ViewModel.executeFlowResultUseCase(
    useCase: suspend () -> Flow<Result<T>>,
    onSuccess: ((T) -> Unit)? = null,
    onFailure: ((Throwable) -> Unit)? = null,
    finally: () -> Unit = {},
) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            useCase()
                .catch { throwable ->
                    withContext(Dispatchers.Main) {
                        throwable.printStackTrace()
                        Log.d("ViewModelExtension", "catch: ${throwable.message}")
                        onFailure?.invoke(throwable)
                    }
                }
                .collect { response ->
                    response.fold(
                        onSuccess = { result ->
                            withContext(Dispatchers.Main) {
                                onSuccess?.invoke(result)
                                finally()
                            }
                        },

                        onFailure = { throwable ->
                            withContext(Dispatchers.Main) {
                                onFailure?.invoke(throwable)
                                finally()
                            }
                        }
                    )
                }
        }
    }
}

suspend fun <T> ViewModel.awaitFlowResult(
    useCase: suspend () -> Flow<Result<T>>
): T = suspendCancellableCoroutine { cont ->
    viewModelScope.launch {
        useCase()
            .catch { throwable ->
                cont.resumeWith(Result.failure(throwable))
            }
            .collect { result ->
                result.fold(
                    onSuccess = { value ->
                        cont.resume(value)
                    },
                    onFailure = { exception ->
                        cont.resumeWith(Result.failure(exception))
                    }
                )
            }
    }
}

fun <T> ViewModel.executeFlowUseCase(
    useCase: suspend () -> Flow<T>,
    onStream: (T) -> Unit,
    onFailure: ((Throwable) -> Unit)? = null,
) {
    viewModelScope.launch {
        withContext(Dispatchers.IO) {
            useCase()
                .catch { throwable ->
                    withContext(Dispatchers.Main) {
                        throwable.printStackTrace()
                        Log.d("ViewModelExtension", "catch: ${throwable.message}")
                        if (onFailure != null) {
                            onFailure(throwable)
                        }
                    }
                }
                .collect { response ->
                    withContext(Dispatchers.Main) {
                        onStream(response)
                    }
                }
        }
    }
}
