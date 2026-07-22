package com.purpletear.aiconversation.data.repository

import android.content.ContentResolver
import android.content.Context
import com.purpletear.aiconversation.data.exception.FailedToCreateConversationDirsException
import com.purpletear.aiconversation.domain.repository.FileManagerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileManagerRepositoryImpl(private val contentResolver: ContentResolver, context: Context) :
    FileManagerRepository {
    private val recordingsDir: File = File(context.filesDir, "ai_coversation/recordings")

    override suspend fun createRecordingFile(): Result<File> {
        return withContext(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ai_conversation_recording_$timestamp.mp3"

            if (!recordingsDir.exists()) {
                try {
                    recordingsDir.mkdirs()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    return@withContext Result.failure(
                        FailedToCreateConversationDirsException(
                            "Failed to create conversation dirs",
                            e
                        )
                    )
                }
            }

            val file = File(recordingsDir, fileName)

            if (!file.exists()) {
                try {
                    file.createNewFile()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    return@withContext Result.failure(
                        FailedToCreateConversationDirsException(
                            "Failed to create conversation dirs",
                            e
                        )
                    )
                }
            }

            return@withContext Result.success(file)
        }
    }

    override suspend fun removeExpiredRecords(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val files = recordingsDir.listFiles()
            files?.forEach {
                if (it.lastModified() < System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7) {
                    try {
                        it.delete()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        return@withContext Result.failure(e)
                    }
                }
            }
            return@withContext Result.success(Unit)
        }
    }

    override suspend fun getTmpCopy(name: String): Result<File> {
        return try {
            val file = File(recordingsDir, name).copyTo(File(recordingsDir, "tmp_$name"))
            Result.success(file)
        } catch (e: FileAlreadyExistsException) {
            Result.success(File(recordingsDir, "tmp_$name"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearDir(): Result<Unit> {
        return try {
            recordingsDir.listFiles()?.forEach {
                it.delete()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}