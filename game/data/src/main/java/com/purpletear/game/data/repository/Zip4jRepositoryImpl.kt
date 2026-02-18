package com.purpletear.game.data.repository

import com.purpletear.sutoko.game.exception.ZipException
import com.purpletear.sutoko.game.model.ExtractZipParams
import com.purpletear.sutoko.game.repository.ZipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException as Zip4jException
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

/**
 * Implementation of [ZipRepository] using the Zip4j library.
 */
class Zip4jRepositoryImpl @Inject constructor() : ZipRepository {

    /**
     * Extracts a zip file to the specified destination using Zip4j.
     *
     * @param params The parameters for extraction.
     * @return A Flow that emits a Boolean indicating whether the extraction was successful.
     */
    override fun extractZip(params: ExtractZipParams): Flow<Result<Unit>> = flow {
        try {
            // Check if the zip file exists
            if (!params.zipFile.exists()) {
                throw ZipException.FileNotFoundException("Zip file not found: ${params.zipFile.absolutePath}")
            }

            // Check if the file is a valid zip file
            if (!params.zipFile.name.endsWith(".zip", ignoreCase = true)) {
                throw ZipException.InvalidZipFileException("File is not a valid zip file: ${params.zipFile.name}")
            }

            // Check if the destination directory exists or can be created
            val destinationDir = java.io.File(params.destinationPath)
            if (!destinationDir.exists() && !destinationDir.mkdirs()) {
                throw ZipException.DestinationPathException("Could not create destination directory: ${params.destinationPath}")
            }

            try {
                val zipFile = params.password?.let { password ->
                    ZipFile(params.zipFile).apply { setPassword(password.toCharArray()) }
                } ?: ZipFile(params.zipFile)

                zipFile.extractAll(params.destinationPath)

                // Delete the archive after extraction if requested
                if (params.deleteArchiveAfterExtraction && params.zipFile.exists()) {
                    if (!params.zipFile.delete()) {
                        // Log warning but don't fail the operation if deletion fails
                        println("Warning: Failed to delete zip file after extraction: ${params.zipFile.absolutePath}")
                    }
                }

                emit(Result.success(Unit))
            } catch (e: Zip4jException) {
                // Handle specific Zip4j exceptions
                val message = e.message?.lowercase() ?: ""
                val exception = when {
                    message.contains("password") && message.contains("required") -> 
                        ZipException.PasswordRequiredException("Password is required for this zip file", e)
                    message.contains("wrong password") || message.contains("invalid password") -> 
                        ZipException.IncorrectPasswordException("Incorrect password for zip file", e)
                    message.contains("invalid") || message.contains("corrupt") -> 
                        ZipException.InvalidZipFileException("Invalid or corrupted zip file", e)
                    else -> 
                        ZipException.ExtractionFailedException("Failed to extract zip file: ${e.message}", e)
                }
                throw exception
            }
        } catch (e: FileNotFoundException) {
            emit(Result.failure(ZipException.FileNotFoundException("Zip file not found: ${params.zipFile.absolutePath}", e)))
        } catch (e: IOException) {
            emit(Result.failure(ZipException.ExtractionFailedException("I/O error during extraction: ${e.message}", e)))
        } catch (e: ZipException) {
            // Our custom exceptions are already properly formatted
            emit(Result.failure(e))
        } catch (e: Exception) {
            // Fallback for any other exceptions
            emit(Result.failure(ZipException.ExtractionFailedException("Unexpected error during extraction: ${e.message}", e)))
        }
    }.flowOn(Dispatchers.IO)
}
