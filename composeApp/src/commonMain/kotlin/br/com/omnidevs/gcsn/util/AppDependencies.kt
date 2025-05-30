package br.com.omnidevs.gcsn.util

interface MediaContentReader {
    suspend fun getMediaBytes(filePath: String): ByteArray
}

object AppDependencies {
    private lateinit var _secureStorage: SecureStorage
    private lateinit var _authService: AuthService
    private lateinit var _mediaContentReader: MediaContentReader

    fun initialize(
        secureStorageProvider: SecureStorageProvider,
        mediaContentReaderProvider: MediaContentReader,
    ) {
        _secureStorage = SecureStorage(secureStorageProvider)
        _authService = AuthService(_secureStorage)
        _mediaContentReader = mediaContentReaderProvider
    }

    val authService: AuthService
        get() {
            if (!::_authService.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _authService
        }


    val mediaContentReader: MediaContentReader
        get() {
            if (!::_mediaContentReader.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _mediaContentReader
        }


}