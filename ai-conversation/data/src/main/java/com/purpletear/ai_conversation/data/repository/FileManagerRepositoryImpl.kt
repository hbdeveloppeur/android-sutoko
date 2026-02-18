package com.purpletear.ai_conversation.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.purpletear.ai_conversation.data.exception.FailedToCreateConversationDirsException
import com.purpletear.ai_conversation.domain.repository.FileManagerRepository
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
                } catch (e: Exception) {
                    return@withContext Result.failure(
                        FailedToCreateConversationDirsException(
                            "Failed to create conversation dirs",
                            e
                        )
                    )
                }
            }

            val metadata = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
                put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Audio.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
                put(MediaStore.Audio.Media.DATE_EXPIRES, System.currentTimeMillis() / 1000)
            }

            contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, metadata)

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
            val file = File(recordingsDir, name)
            Result.success(file)
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}