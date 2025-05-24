package br.com.omnidevs.gcsn.util

object AppDependencies {
    private lateinit var _secureStorage: SecureStorage
    private lateinit var _authService: AuthService

    fun initialize(secureStorageProvider: SecureStorageProvider) {
        _secureStorage = SecureStorage(secureStorageProvider)
        _authService = AuthService(_secureStorage)
    }

    val authService: AuthService get() {
        if (!::_authService.isInitialized) {
            throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
        }
        return _authService
    }
}