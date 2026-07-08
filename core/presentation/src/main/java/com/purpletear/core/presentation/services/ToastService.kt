package com.purpletear.core.presentation.services

interface ToastService {
    operator fun invoke(message: Int, vararg formatArgs: Any?)
}
