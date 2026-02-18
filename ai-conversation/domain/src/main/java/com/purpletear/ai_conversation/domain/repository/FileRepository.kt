package com.purpletear.ai_conversation.domain.repository

import android.graphics.Bitmap
import java.io.File

interface FileRepository {
    fun save(bitmap: Bitmap): Result<File>

}