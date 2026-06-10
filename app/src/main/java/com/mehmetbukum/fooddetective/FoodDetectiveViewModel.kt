package com.mehmetbukum.fooddetective

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetbukum.fooddetective.data.Additive
import com.mehmetbukum.fooddetective.data.AdditiveDataSource
import com.mehmetbukum.fooddetective.data.AdditiveRepository
import com.mehmetbukum.fooddetective.data.AdditivesVersionResponse
import com.mehmetbukum.fooddetective.data.OcrSearchResult
import com.mehmetbukum.fooddetective.data.SyncResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ApiConnectionStatus {
    CHECKING,
    ONLINE,
    LOCAL,
    OFFLINE
}

data class FoodDetectiveUiState(
    val searchQuery: String = "",
    val suggestions: List<Additive> = emptyList(),
    val singleResult: Additive? = null,
    val ocrResult: OcrSearchResult? = null,
    val hasSearched: Boolean = false,
    val errorMessage: UiText? = null,
    val syncMessage: UiText? = null,
    val isLoading: Boolean = false,
    val showCamera: Boolean = false,
    val apiConnectionStatus: ApiConnectionStatus = ApiConnectionStatus.CHECKING
)

class FoodDetectiveViewModel(
    private val repository: AdditiveDataSource,
    private val syncRepository: AdditiveRepository? = repository as? AdditiveRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val suggestionDelayMillis: Long = 220L
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodDetectiveUiState())
    val uiState: StateFlow<FoodDetectiveUiState> = _uiState.asStateFlow()

    private var suggestionJob: Job? = null
    private var syncJob: Job? = null

    fun setApiConnectionStatus(status: ApiConnectionStatus) {
        _uiState.update { it.copy(apiConnectionStatus = status) }
    }

    fun refreshSyncMessageTime(lastSuccessfulSyncText: String?) {
        if (lastSuccessfulSyncText.isNullOrBlank()) return

        _uiState.update { state ->
            val currentMessage = state.syncMessage
            if (currentMessage is UiText.Resource && currentMessage.resId in SYNC_MESSAGES_WITH_TIME) {
                state.copy(syncMessage = currentMessage.copy(args = listOf(lastSuccessfulSyncText)))
            } else {
                state
            }
        }
    }

    fun runScheduledApiSync(
        shouldCheckSync: Boolean,
        lastSuccessfulVersionHash: String?,
        lastSuccessfulSyncText: String?,
        onSuccessfulSync: (AdditivesVersionResponse) -> String?
    ) {
        if (!shouldCheckSync) {
            Log.i(TAG, "Son başarılı API senkronizasyonu yeni; yerel veritabanı kullanılacak.")
            _uiState.update {
                it.copy(
                    apiConnectionStatus = ApiConnectionStatus.LOCAL,
                    syncMessage = lastSuccessfulSyncText?.let { syncText ->
                        UiText.Resource(R.string.sync_local_last_updated_at, listOf(syncText))
                    } ?: UiText.Resource(R.string.sync_local_never_updated)
                )
            }
            return
        }

        val remoteSyncRepository = syncRepository
        if (remoteSyncRepository == null) {
            _uiState.update {
                it.copy(
                    apiConnectionStatus = ApiConnectionStatus.OFFLINE,
                    syncMessage = UiText.Resource(R.string.sync_warning_unavailable)
                )
            }
            return
        }

        syncJob?.cancel()
        _uiState.update {
            it.copy(
                apiConnectionStatus = ApiConnectionStatus.CHECKING,
                syncMessage = null
            )
        }

        syncJob = viewModelScope.launch {
            val syncResult = withContext(ioDispatcher) {
                remoteSyncRepository.syncFromApi(lastSuccessfulVersionHash = lastSuccessfulVersionHash)
            }
            logSyncResult(syncResult)

            val successfulSyncText = when (syncResult) {
                is SyncResult.Success -> onSuccessfulSync(syncResult.version)
                is SyncResult.NoChange -> onSuccessfulSync(syncResult.version)
                is SyncResult.Error,
                is SyncResult.Skipped -> null
            }

            _uiState.update {
                it.copy(
                    apiConnectionStatus = syncResult.toApiConnectionStatus(),
                    syncMessage = SyncMessageMapper.toUserMessage(syncResult, successfulSyncText)
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                singleResult = null,
                ocrResult = null,
                hasSearched = false,
                errorMessage = null
            )
        }
        loadSuggestions(query)
    }

    fun search(queryOverride: String? = null) {
        val query = (queryOverride ?: _uiState.value.searchQuery).trim()
        if (query.isBlank()) return

        suggestionJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = query,
                suggestions = emptyList(),
                singleResult = null,
                ocrResult = null,
                hasSearched = false,
                errorMessage = null,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    repository.searchSingle(query)
                }
                _uiState.update {
                    it.copy(
                        singleResult = result,
                        hasSearched = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.localizedMessage
                            ?.takeIf { message -> message.isNotBlank() }
                            ?.let(UiText::Dynamic)
                            ?: UiText.Resource(R.string.error_unknown_search),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectSuggestion(additive: Additive) {
        suggestionJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = additive.code,
                suggestions = emptyList(),
                singleResult = additive,
                ocrResult = null,
                errorMessage = null,
                hasSearched = true,
                isLoading = false
            )
        }
    }

    fun openCamera() {
        _uiState.update { it.copy(showCamera = true) }
    }

    fun closeCamera() {
        _uiState.update { it.copy(showCamera = false) }
    }

    fun setCameraError(message: UiText) {
        _uiState.update {
            it.copy(
                showCamera = false,
                errorMessage = message,
                isLoading = false
            )
        }
    }

    fun setCameraError(message: String) {
        setCameraError(
            message
                .takeIf { it.isNotBlank() }
                ?.let(UiText::Dynamic)
                ?: UiText.Resource(R.string.error_camera_generic)
        )
    }

    fun searchFromOcr(recognizeText: suspend () -> String) {
        suggestionJob?.cancel()
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                singleResult = null,
                ocrResult = null,
                suggestions = emptyList(),
                hasSearched = false,
                showCamera = false
            )
        }

        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) {
                    repository.searchFromOcr(recognizeText())
                }
                _uiState.update {
                    it.copy(
                        ocrResult = result,
                        hasSearched = true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.localizedMessage
                            ?.takeIf { message -> message.isNotBlank() }
                            ?.let(UiText::Dynamic)
                            ?: UiText.Resource(R.string.error_ocr_failed),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadSuggestions(query: String) {
        suggestionJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        suggestionJob = viewModelScope.launch {
            delay(suggestionDelayMillis)
            try {
                val results = withContext(ioDispatcher) {
                    repository.searchSuggestions(trimmed)
                }
                _uiState.update { it.copy(suggestions = results) }
            } catch (e: Exception) {
                _uiState.update { it.copy(suggestions = emptyList()) }
            }
        }
    }

    private fun SyncResult.toApiConnectionStatus(): ApiConnectionStatus {
        return when (this) {
            is SyncResult.Success,
            is SyncResult.NoChange -> ApiConnectionStatus.ONLINE

            is SyncResult.Error,
            is SyncResult.Skipped -> ApiConnectionStatus.OFFLINE
        }
    }

    private fun logSyncResult(syncResult: SyncResult) {
        when (syncResult) {
            is SyncResult.Success -> Log.i(
                TAG,
                "Katkı maddesi API senkronizasyonu tamamlandı. Sunucudan alınan kayıt: ${syncResult.updatedCount}, hash: ${syncResult.version.version_hash.orEmpty()}"
            )

            is SyncResult.NoChange -> Log.i(
                TAG,
                "Katkı maddesi veritabanı güncel. Hash değişmedi: ${syncResult.version.version_hash.orEmpty()}"
            )

            is SyncResult.Skipped -> Log.w(
                TAG,
                "Katkı maddesi API senkronizasyonu atlandı: ${syncResult.reason}"
            )

            is SyncResult.Error -> Log.w(
                TAG,
                "Katkı maddesi API senkronizasyonu başarısız; yerel veritabanı kullanılacak: ${syncResult.reason}"
            )
        }
    }

    companion object {
        private const val TAG = "EDetectiveSync"
        private val SYNC_MESSAGES_WITH_TIME = setOf(
            R.string.sync_success_updated_at,
            R.string.sync_success_checked_at,
            R.string.sync_local_last_updated_at
        )
    }
}
