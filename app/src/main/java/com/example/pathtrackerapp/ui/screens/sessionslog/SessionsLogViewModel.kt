package com.example.pathtrackerapp.ui.screens.sessionslog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pathtrackerapp.domain.model.TrackingSession
import com.example.pathtrackerapp.domain.repository.TrackingSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionsLogUiState(
    val display: DisplaySessionsLogs = DisplaySessionsLogs.Loading,
)

sealed interface DisplaySessionsLogs {
    object Loading : DisplaySessionsLogs
    object Empty : DisplaySessionsLogs
    data class Success(val sessions: List<TrackingSession>) : DisplaySessionsLogs
    data class Error(val message: String) : DisplaySessionsLogs
}


@HiltViewModel
class SessionsLogViewModel @Inject constructor(
    private val sessionRepository: TrackingSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsLogUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeSessions()
    }

    fun observeSessions(){

        _uiState.update {
            it.copy(display = DisplaySessionsLogs.Loading)
        }

        viewModelScope.launch {
            try {
                sessionRepository.observeAllSessionsFlow()
                    .collect { sessions ->
                        _uiState.value = if (sessions.isEmpty()) {
                            SessionsLogUiState(display = DisplaySessionsLogs.Empty)
                        } else {
                            SessionsLogUiState(display = DisplaySessionsLogs.Success(sessions))
                        }
                    }
            } catch (e: Exception){
                _uiState.update {
                    it.copy(display = DisplaySessionsLogs.Error(e.localizedMessage ?: "Unknown error"))
                }
            }
        }
    }
}