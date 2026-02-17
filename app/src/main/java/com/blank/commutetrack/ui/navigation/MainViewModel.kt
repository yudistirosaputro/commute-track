package com.blank.commutetrack.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BottomBarState(
    val visibleRoutes: Set<String> = setOf("dashboard", "statistics", "tracking", "history", "settings")
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _bottomBarState = MutableStateFlow(BottomBarState())
    val bottomBarState: StateFlow<BottomBarState> = _bottomBarState.asStateFlow()

    fun updateBottomBarVisibility(isVisible: Boolean) {
        _bottomBarState.value = _bottomBarState.value
    }
}
