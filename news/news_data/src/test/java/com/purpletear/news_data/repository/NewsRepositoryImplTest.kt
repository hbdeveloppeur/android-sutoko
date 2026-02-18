package com.purpletear.news_data.repository

import com.purpletear.news_data.remote.NewsApiWrapper
import com.purpletear.sutoko.core.domain.helper.AppVersionProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Before

class NewsRepositoryImplTest {

    private lateinit var apiWrapper: NewsApiWrapper
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var repository: NewsRepositoryImpl

    @Before
    fun setup() {
        apiWrapper = mockk()
        appVersionProvider = mockk()
        every { appVersionProvider.getVersionCode() } returns 1
        repository = NewsRepositoryImpl(apiWrapper, appVersionProvider)
    }
}
