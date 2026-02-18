package com.purpletear.core.image_downloader

import kotlinx.coroutines.flow.Flow


interface ImageDownloader {
    fun download(url: String): Flow<Result<Unit>>
}