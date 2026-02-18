package com.purpletear.aiconversation.domain.usecase

import com.purpletear.aiconversation.domain.model.messages.entities.MessageVocal
import java.io.File
import javax.inject.Inject

class TransformFileToVoiceMessageUseCase @Inject constructor() {
    operator fun invoke(
        file: File,
    ): MessageVocal {
        return MessageVocal(
            file = file
        )
    }
}