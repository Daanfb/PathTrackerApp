package com.example.pathtrackerapp.ui.screens.trackingsummary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.pathtrackerapp.domain.model.TrackingSession
import com.example.pathtrackerapp.domain.repository.TrackingSessionRepository
import com.example.pathtrackerapp.ui.navigation.TrackingSessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class TitleTrackingUiState(
    val session: TrackingSession,
    val isTitleEmpty: Boolean = false
)

@HiltViewModel
class TrackingSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: TrackingSessionRepository
) : ViewModel() {

    private val encodedSummarySession =
        savedStateHandle.toRoute<TrackingSessionSummary>().summarySession
    private val summarySession = Json.decodeFromString<TrackingSession>(encodedSummarySession)

    private val _uiState = MutableStateFlow(TitleTrackingUiState(session = summarySession))
    val uiState = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update {
            it.copy(
                session = it.session.copy(title = newTitle),
                isTitleEmpty = false
            )
        }
    }

    fun onNeededTitleDialogDismiss() {
        _uiState.update { it.copy(isTitleEmpty = false) }
    }

    fun onSaveClick(navToLog: () -> Unit) {
        val currentTitle = _uiState.value.session.title

        if (currentTitle.isBlank()) {
            _uiState.update { it.copy(isTitleEmpty = true) }
            return
        }

        viewModelScope.launch {
            sessionRepository.insertSession(_uiState.value.session)
        }

        navToLog()
    }
}