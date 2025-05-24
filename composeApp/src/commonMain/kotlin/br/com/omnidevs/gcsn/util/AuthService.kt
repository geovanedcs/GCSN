package br.com.omnidevs.gcsn.util

import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.network.api.BlueskyAuthApi

class AuthService(private val secureStorage: SecureStorage) {
    fun isUserAuthenticated(): Boolean {
        return secureStorage.isAuthenticated()
    }

    fun getUserData(): UserData? {
        val authToken = secureStorage.getAuthToken() ?: return null
        val refreshToken = secureStorage.getRefreshToken() ?: return null
        val did = secureStorage.getUserDid() ?: return null
        val handle = secureStorage.getUserHandle() ?: return null

        return UserData(authToken, refreshToken, did, handle)
    }

    suspend fun validateToken(): Boolean {
        try {
            AuthManager.accessToken = secureStorage.getAuthToken()
            AuthManager.refreshToken = secureStorage.getRefreshToken()

            if (AuthManager.accessToken.isNullOrEmpty()) {
                secureStorage.clearAll()
                return false
            }

            val authApi = BlueskyAuthApi()
            val isValid = authApi.validateToken()

            if (!isValid) {
                secureStorage.clearAll()
            }

            return isValid
        } catch (e: Exception) {
            secureStorage.clearAll()
            return false
        }
    }

    fun saveUserData(authToken: String, refreshToken: String, did: String, handle: String) {
        secureStorage.setAuthToken(authToken)
        secureStorage.setRefreshToken(refreshToken)
        secureStorage.setUserDid(did)
        secureStorage.setUserHandle(handle)
    }

    data class UserData(
        val authToken: String,
        val refreshToken: String,
        val did: String,
        val handle: String
    )

    companion object {
        private var instance: AuthService? = null

        fun getInstance(secureStorage: SecureStorage): AuthService {
            if (instance == null) {
                instance = AuthService(secureStorage)
            }
            return instance!!
        }
    }
}