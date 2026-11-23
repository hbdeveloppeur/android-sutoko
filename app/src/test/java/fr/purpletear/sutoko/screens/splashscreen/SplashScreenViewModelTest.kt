package fr.purpletear.sutoko.screens.splashscreen

import androidx.lifecycle.ViewModelStore
import fr.purpletear.sutoko.symbols.SymbolsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import purpletear.fr.purpleteartools.TableOfSymbols

@OptIn(ExperimentalCoroutinesApi::class)
class SplashScreenViewModelTest {
    private val store = ViewModelStore()

    @Before
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `loaded data does not skip the video`() = runTest {
        val viewModel = viewModel(FakeSymbolsRepository(TableOfSymbols(1)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isReadyToNavigate.collect {}
        }
        runCurrent()
        assertFalse(viewModel.isReadyToNavigate.value)
        viewModel.setAnimationsFinished()
        runCurrent()
        assertTrue(viewModel.isReadyToNavigate.value)
    }

    @Test
    fun `finished video still waits for required data`() = runTest {
        val repository = FakeSymbolsRepository(null)
        val viewModel = viewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.isReadyToNavigate.collect {}
        }
        viewModel.setAnimationsFinished()
        runCurrent()
        assertFalse(viewModel.isReadyToNavigate.value)
        repository.symbols.value = TableOfSymbols(1)
        runCurrent()
        assertTrue(viewModel.isReadyToNavigate.value)
    }

    private fun viewModel(repository: SymbolsRepository) = SplashScreenViewModel(repository).also {
        store.put("splash", it)
    }

    private class FakeSymbolsRepository(initial: TableOfSymbols?) : SymbolsRepository {
        override val symbols = MutableStateFlow(initial)
        override suspend fun load() = requireNotNull(symbols.value)
    }
}
