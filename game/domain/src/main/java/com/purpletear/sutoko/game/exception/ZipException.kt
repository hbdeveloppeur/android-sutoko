package com.purpletear.sutoko.game.exception

/**
 * Base exception class for zip-related operations.
 */
sealed class ZipException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Exception thrown when the zip file is not found.
     */
    class FileNotFoundException(message: String = "Zip file not found", cause: Throwable? = null) : 
        ZipException(message, cause)
    
    /**
     * Exception thrown when the file is not a valid zip file.
     */
    class InvalidZipFileException(message: String = "Invalid zip file format", cause: Throwable? = null) : 
        ZipException(message, cause)
    
    /**
     * Exception thrown when a password is required but not provided.
     */
    class PasswordRequiredException(message: String = "Password is required for this zip file", cause: Throwable? = null) : 
        ZipException(message, cause)
    
    /**
     * Exception thrown when the provided password is incorrect.
     */
    class IncorrectPasswordException(message: String = "Incorrect password for zip file", cause: Throwable? = null) : 
        ZipException(message, cause)
    
    /**
     * Exception thrown when there are issues with the destination path.
     */
    class DestinationPathException(message: String = "Issue with destination path", cause: Throwable? = null) : 
        ZipException(message, cause)
    
    /**
     * Exception thrown for general extraction failures.
     */
    class ExtractionFailedException(message: String = "Failed to extract zip file", cause: Throwable? = null) : 
        ZipException(message, cause)
}