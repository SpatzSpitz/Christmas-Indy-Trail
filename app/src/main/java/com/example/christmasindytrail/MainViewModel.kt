package com.example.christmasindytrail

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.christmasindytrail.data.Post
import com.example.christmasindytrail.data.Trail
import com.example.christmasindytrail.data.TrailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "progress")

private val PROGRESS_KEY = intPreferencesKey("progress_index")

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TrailRepository(application.applicationContext)
    private val dataStore = application.applicationContext.dataStore

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val trail = repository.loadTrail()
            _uiState.update { it.copy(trail = trail, isLoading = false, showIntro = true) }
            // Reset progress on every app start as requested
            saveProgress(0)
            dataStore.data
                .map { prefs -> prefs[PROGRESS_KEY] ?: 0 }
                .distinctUntilChanged()
                .collect { index ->
                    _uiState.update { state ->
                        val capped = index.coerceIn(0, trail.posts.size)
                        state.copy(
                            progressIndex = capped,
                            showScanner = capped >= trail.posts.size || state.showScanner,
                            showIntro = state.showIntro && capped == 0
                        )
                    }
                }
        }
    }

    fun handleScan(rawValue: String) {
        val trail = _uiState.value.trail ?: return
        val match = QR_PATTERN.matchEntire(rawValue.trim())
        if (match == null) {
            postMessage("Ungueltiger QR-Code")
            return
        }
        val scannedId = match.groupValues[1].padStart(2, '0')
        val expectedIndex = _uiState.value.progressIndex.coerceAtMost(trail.posts.lastIndex)
        val expectedId = trail.posts.getOrNull(expectedIndex)?.id
        if (expectedId == null) {
            postMessage("Keine Posten gefunden")
            return
        }
        if (scannedId != expectedId) {
            postMessage("Das ist noch nicht der richtige Posten.")
            return
        }
        val post = trail.posts.firstOrNull { it.id == scannedId }
        if (post != null) {
            viewModelScope.launch {
                saveProgress(expectedIndex + 1)
                _uiState.update {
                    it.copy(
                        currentPost = post,
                        showScanner = false,
                        revealedHints = 1,
                        message = "Posten $scannedId freigeschaltet"
                    )
                }
            }
        }
    }

    fun revealNextHint() {
        val currentPost = _uiState.value.currentPost ?: return
        _uiState.update { state ->
            val next = (state.revealedHints + 1).coerceAtMost(currentPost.hintFiles.size)
            state.copy(revealedHints = next)
        }
    }

    fun goToScanner() {
        val trail = _uiState.value.trail ?: return
        val progress = _uiState.value.progressIndex
        if (progress >= trail.posts.size) return
        _uiState.update { it.copy(currentPost = null, showScanner = true, revealedHints = 1, showIntro = false) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            saveProgress(0)
            _uiState.update { it.copy(currentPost = null, showScanner = false, revealedHints = 1, showIntro = true, message = "Fortschritt zurueckgesetzt") }
        }
    }

    fun startFromIntro() {
        _uiState.update { it.copy(showIntro = false, showScanner = true) }
    }

    private fun postMessage(text: String) {
        _uiState.update { it.copy(message = text) }
    }

    private suspend fun saveProgress(index: Int) {
        dataStore.edit { prefs ->
            prefs[PROGRESS_KEY] = index
        }
    }

    companion object {
        private val QR_PATTERN = Regex("^indypath://posten/([0-9]+)$", RegexOption.IGNORE_CASE)
    }
}

data class UiState(
    val trail: Trail? = null,
    val progressIndex: Int = 0,
    val currentPost: Post? = null,
    val showScanner: Boolean = true,
    val showIntro: Boolean = false,
    val revealedHints: Int = 1,
    val message: String? = null,
    val isLoading: Boolean = false
)
