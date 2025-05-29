package br.com.omnidevs.gcsn.util

import dev.icerock.moko.media.picker.MediaPickerController
import dev.icerock.moko.permissions.PermissionsController // Adicionar importação

interface MediaContentReader {
    suspend fun getMediaBytes(filePath: String): ByteArray
}

object AppDependencies {
    private lateinit var _secureStorage: SecureStorage
    private lateinit var _authService: AuthService
    private lateinit var _mediaPickerController: MediaPickerController
    private lateinit var _mediaContentReader: MediaContentReader
    private lateinit var _permissionsController: PermissionsController // Adicionar esta linha

    fun initialize(
        secureStorageProvider: SecureStorageProvider,
        mediaPickerControllerProvider: MediaPickerController,
        mediaContentReaderProvider: MediaContentReader,
        permissionsControllerProvider: PermissionsController // Adicionar parâmetro
    ) {
        _secureStorage = SecureStorage(secureStorageProvider)
        _authService = AuthService(_secureStorage)
        _mediaPickerController = mediaPickerControllerProvider
        _mediaContentReader = mediaContentReaderProvider
        _permissionsController = permissionsControllerProvider // Adicionar esta linha
    }

    val authService: AuthService
        get() {
            if (!::_authService.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _authService
        }

    val mediaPickerController: MediaPickerController
        get() {
            if (!::_mediaPickerController.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _mediaPickerController
        }

    val mediaContentReader: MediaContentReader
        get() {
            if (!::_mediaContentReader.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _mediaContentReader
        }

    val permissionsController: PermissionsController // Adicionar este getter
        get() {
            if (!::_permissionsController.isInitialized) {
                throw IllegalStateException("AppDependencies not initialized. Call initialize() first.")
            }
            return _permissionsController
        }
}