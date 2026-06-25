package fr.purpletear.sutoko.sync.news

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.purpletear.sutoko.news.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsSyncCoordinatorTest {

    private val newsRepository: NewsRepository = mockk(relaxed = true)
    private val coordinator = NewsSyncCoordinator(newsRepository)

    @Test
    fun `sync is called when lifecycle moves to STARTED`() = runTest {
        val lifecycleOwner = TestLifecycleOwner()
        coordinator.start(lifecycleOwner.lifecycle, TestScope(UnconfinedTestDispatcher()))

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        coVerify(exactly = 1) { newsRepository.syncNews(any()) }
    }

    @Test
    fun `sync failures are swallowed`() = runTest {
        coEvery { newsRepository.syncNews(any()) } returns Result.failure(RuntimeException("boom"))
        val lifecycleOwner = TestLifecycleOwner()
        coordinator.start(lifecycleOwner.lifecycle, TestScope(UnconfinedTestDispatcher()))

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

        coVerify(exactly = 1) { newsRepository.syncNews(any()) }
    }

    private class TestLifecycleOwner : androidx.lifecycle.LifecycleOwner {
        private val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle = registry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }
}
