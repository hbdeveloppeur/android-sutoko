package com.purpletear.ntfy

/**
 * Custom exception for Ntfy-related errors
 */
class NtfyException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
