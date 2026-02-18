package com.purpletear.aiconversation.domain.repository

import java.io.File

interface FileManagerRepository {
    suspend fun createRecordingFile(): Result<File>
    suspend fun removeExpiredRecords(): Result<Unit>
    suspend fun getTmpCopy(name: String): Result<File>
    suspend fun clearDir(): Result<Unit>
}