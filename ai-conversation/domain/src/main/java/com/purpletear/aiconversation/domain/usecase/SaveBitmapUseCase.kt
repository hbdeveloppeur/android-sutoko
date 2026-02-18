package com.purpletear.aiconversation.domain.usecase

import android.graphics.Bitmap
import com.purpletear.aiconversation.domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class SaveBitmapUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    operator fun invoke(
        bitmap: Bitmap
    ): Result<File> {
        return fileRepository.save(bitmap = bitmap)
    }
}