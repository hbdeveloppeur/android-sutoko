package com.purpletear.ai_conversation.data.repository

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.purpletear.ai_conversation.domain.repository.FileManagerRepository
import com.purpletear.ai_conversation.domain.repository.MicrophoneRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class MicrophoneRepositoryImpl(
    private val context: Context,
    private val fileManager: FileManagerRepository
) : MicrophoneRepository {
    private var recorder: MediaRecorder? = null
    private var file: File? = null


    private fun bestSampleRates(): Int {
        val sampleRates = arrayOf(
            8000,
            11025,
            16000,
            22050,
            44100
        )  // array of potential sample rates
        var maxSampleRate = 8000  // a default/fallback value

        for (rate in sampleRates) {
            val bufferSize = AudioRecord.getMinBufferSize(
                rate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                // this sample rate could be set up successfully, save it as the new maximum
                maxSampleRate = rate
            }
        }
        return maxSampleRate
    }

    override suspend fun startRecording(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            file = fileManager.createRecordingFile().getOrThrow()
            if (!file!!.exists()) {
                throw IOException("File not found")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder = MediaRecorder(context).apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // MPEG-4 generally has better quality than 3GP
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(bestSampleRates()) // A higher sampling rate means better audio quality, 44100 is a commonly used sampling rate
                    setAudioEncodingBitRate(128000) // Higher bit rates result in better quality but larger files
                    setOutputFile(file!!.absolutePath)
                    prepare()
                    start()
                }
            } else {
                @Suppress("DEPRECATION")
                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // MPEG-4 generally has better quality than 3GP
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(bestSampleRates()) // A higher sampling rate means better audio quality, 44100 is a commonly used sampling rate
                    setAudioEncodingBitRate(128000) // Higher bit rates result in better quality but larger files
                    setOutputFile(file!!.absolutePath)
                    prepare()
                    start()
                }
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun stopRecording(): Result<Unit> {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecordingFile(): File {
        if (file == null || !file!!.exists()) {
            throw IllegalStateException("File is null")
        }
        return fileManager.getTmpCopy(file!!.name).getOrThrow()
    }

    override fun isRecording(): Boolean {
        return recorder != null
    }

    override suspend fun clearRecordingDir(): Result<Unit> {
        return fileManager.clearDir()
    }

}