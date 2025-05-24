package br.com.omnidevs.gcsn.util

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

expect class SecureStorageProvider {
    fun getSecureSettings(): Settings
}

class SecureStorage(private val provider: SecureStorageProvider) {
    private val settings = provider.getSecureSettings()

    private val _authStateFlow = MutableStateFlow(isAuthenticated())
    val authState: StateFlow<Boolean> = _authStateFlow.asStateFlow()

    // Auth token
    fun getAuthToken(): String? = settings.getStringOrNull(AUTH_TOKEN_KEY)

    fun setAuthToken(token: String?) {
        if (token == null) settings.remove(AUTH_TOKEN_KEY)
        else settings.putString(AUTH_TOKEN_KEY, token)
        _authStateFlow.value = isAuthenticated()
    }

    // Refresh token
    fun getRefreshToken(): String? = settings.getStringOrNull(REFRESH_TOKEN_KEY)

    fun setRefreshToken(token: String?) {
        if (token == null) settings.remove(REFRESH_TOKEN_KEY)
        else settings.putString(REFRESH_TOKEN_KEY, token)
    }

    // User DID
    fun getUserDid(): String? = settings.getStringOrNull(USER_DID_KEY)

    fun setUserDid(did: String?) {
        if (did == null) settings.remove(USER_DID_KEY)
        else settings.putString(USER_DID_KEY, did)
    }

    // User handle
    fun getUserHandle(): String? = settings.getStringOrNull(USER_HANDLE_KEY)

    fun setUserHandle(handle: String?) {
        if (handle == null) settings.remove(USER_HANDLE_KEY)
        else settings.putString(USER_HANDLE_KEY, handle)
    }

    fun isAuthenticated(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    fun clearAll() {
        settings.remove(AUTH_TOKEN_KEY)
        settings.remove(REFRESH_TOKEN_KEY)
        settings.remove(USER_DID_KEY)
        settings.remove(USER_HANDLE_KEY)
        _authStateFlow.value = false
    }

    companion object {
        const val AUTH_TOKEN_KEY = "auth_token"
        const val REFRESH_TOKEN_KEY = "refresh_token"
        const val USER_DID_KEY = "user_did"
        const val USER_HANDLE_KEY = "user_handle"
    }
}