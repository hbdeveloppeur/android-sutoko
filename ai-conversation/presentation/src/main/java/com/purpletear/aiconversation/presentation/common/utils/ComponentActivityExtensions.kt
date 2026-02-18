package com.purpletear.aiconversation.presentation.common.utils

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun <T> ComponentActivity.executeFlowUseCase(
    useCase: suspend () -> Flow<T>,
    onStream: (T) -> Unit,
    onFailure: ((Throwable) -> Unit)? = null,
) {
    lifecycleScope.launch {
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
