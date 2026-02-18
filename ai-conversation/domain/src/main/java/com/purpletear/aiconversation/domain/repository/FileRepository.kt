package com.purpletear.aiconversation.domain.repository

import android.graphics.Bitmap
import java.io.File

interface FileRepository {
    fun save(bitmap: Bitmap): Result<File>

}