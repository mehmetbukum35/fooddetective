package com.mehmetbukum.fooddetective

import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.AdditiveDataSource
import com.mehmetbukum.fooddetective.data.OcrSearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FoodDetectiveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun onSearchQueryChange_updatesQueryAndLoadsSuggestions() = runTest {
        val additive = additive(code = "E160A", name = "Karoten")
        val repository = FakeAdditiveDataSource(
            suggestionsResult = listOf(additive)
        )
        val viewModel = createViewModel(repository)

        viewModel.onSearchQueryChange("160")

        val state = viewModel.uiState.value
        assertEquals("160", state.searchQuery)
        assertEquals(listOf(additive), state.suggestions)
        assertFalse(state.hasSearched)
        assertNull(state.errorMessage)
    }

    @Test
    fun search_withExistingCode_updatesSingleResultAndStopsLoading() = runTest {
        val additive = additive(code = "E330", name = "Sitrik Asit")
        val repository = FakeAdditiveDataSource(
            singleResult = additive
        )
        val viewModel = createViewModel(repository)

        viewModel.search("e330")

        val state = viewModel.uiState.value
        assertEquals("e330", state.searchQuery)
        assertSame(additive, state.singleResult)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(emptyList<Additive>(), state.suggestions)
    }

    @Test
    fun search_withBlankQuery_doesNothing() = runTest {
        val repository = FakeAdditiveDataSource()
        val viewModel = createViewModel(repository)

        viewModel.search("   ")

        val state = viewModel.uiState.value
        assertEquals(FoodDetectiveUiState(), state)
        assertEquals(0, repository.searchSingleCalls)
    }

    @Test
    fun search_whenRepositoryThrows_updatesDynamicErrorState() = runTest {
        val repository = FakeAdditiveDataSource(
            searchError = IllegalStateException("Veritabanı hatası")
        )
        val viewModel = createViewModel(repository)

        viewModel.search("E120")

        val state = viewModel.uiState.value
        assertEquals(UiText.Dynamic("Veritabanı hatası"), state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.hasSearched)
        assertNull(state.singleResult)
    }

    @Test
    fun search_whenRepositoryThrowsBlankMessage_usesResourceErrorState() = runTest {
        val repository = FakeAdditiveDataSource(
            searchError = IllegalStateException("")
        )
        val viewModel = createViewModel(repository)

        viewModel.search("E120")

        val state = viewModel.uiState.value
        assertEquals(UiText.Resource(R.string.error_unknown_search), state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun selectSuggestion_setsSelectedAdditiveAsSingleResult() = runTest {
        val additive = additive(code = "E120", name = "Karmin")
        val viewModel = createViewModel(FakeAdditiveDataSource())

        viewModel.selectSuggestion(additive)

        val state = viewModel.uiState.value
        assertEquals("E120", state.searchQuery)
        assertSame(additive, state.singleResult)
        assertEquals(emptyList<Additive>(), state.suggestions)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
    }

    @Test
    fun cameraState_openCloseAndErrorWorkCorrectly() = runTest {
        val viewModel = createViewModel(FakeAdditiveDataSource())

        viewModel.openCamera()
        assertTrue(viewModel.uiState.value.showCamera)

        viewModel.closeCamera()
        assertFalse(viewModel.uiState.value.showCamera)

        viewModel.openCamera()
        viewModel.setCameraError(UiText.Resource(R.string.error_camera_start_failed))

        val state = viewModel.uiState.value
        assertFalse(state.showCamera)
        assertEquals(UiText.Resource(R.string.error_camera_start_failed), state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun cameraState_blankStringErrorFallsBackToResourceError() = runTest {
        val viewModel = createViewModel(FakeAdditiveDataSource())

        viewModel.setCameraError("")

        assertEquals(
            UiText.Resource(R.string.error_camera_generic),
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun searchFromOcr_updatesOcrResultAndClosesCamera() = runTest {
        val ocrResult = OcrSearchResult(
            found = listOf(additive(code = "E330", name = "Sitrik Asit")),
            notFoundCodes = listOf("E999"),
            totalDetected = 2,
            rawText = "E330 E999"
        )
        val repository = FakeAdditiveDataSource(ocrResult = ocrResult)
        val viewModel = createViewModel(repository)

        viewModel.openCamera()
        viewModel.searchFromOcr { "E330 E999" }

        val state = viewModel.uiState.value
        assertEquals(ocrResult, state.ocrResult)
        assertTrue(state.hasSearched)
        assertFalse(state.isLoading)
        assertFalse(state.showCamera)
        assertNull(state.errorMessage)
    }

    private fun createViewModel(repository: AdditiveDataSource): FoodDetectiveViewModel {
        return FoodDetectiveViewModel(
            repository = repository,
            ioDispatcher = testDispatcher,
            suggestionDelayMillis = 0L
        )
    }

    private fun additive(code: String, name: String): Additive {
        return Additive(
            code = code,
            name_tr = name,
            functional_class = "Renklendirici",
            halal_status = "Helal",
            health_status = "Düşük risk",
            risk_level = "Düşük",
            description = "Test açıklaması",
            warning = null,
            name_en = null,
            functional_class_en = null,
            health_status_en = null,
            description_en = null,
            warning_en = null
        )
    }

    private class FakeAdditiveDataSource(
        private val singleResult: Additive? = null,
        private val suggestionsResult: List<Additive> = emptyList(),
        private val ocrResult: OcrSearchResult = OcrSearchResult(
            found = emptyList(),
            notFoundCodes = emptyList(),
            totalDetected = 0,
            rawText = ""
        ),
        private val searchError: Exception? = null
    ) : AdditiveDataSource {

        var searchSingleCalls: Int = 0
            private set

        override suspend fun searchSingle(query: String): Additive? {
            searchSingleCalls++
            searchError?.let { throw it }
            return singleResult
        }

        override suspend fun searchSuggestions(query: String, limit: Int): List<Additive> {
            return suggestionsResult.take(limit)
        }

        override suspend fun searchFromOcr(rawText: String): OcrSearchResult {
            return ocrResult.copy(rawText = rawText)
        }
    }
}
