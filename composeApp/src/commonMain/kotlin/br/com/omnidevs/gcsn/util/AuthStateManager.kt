package br.com.omnidevs.gcsn.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuthState {
    LOGGED_IN,
    LOGGED_OUT,
    TOKEN_REFRESHING
}

object AuthStateManager {
    private val _authState = MutableStateFlow<AuthState>(AuthState.LOGGED_OUT)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
}