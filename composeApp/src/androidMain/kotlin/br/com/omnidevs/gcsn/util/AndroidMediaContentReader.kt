package br.com.omnidevs.gcsn.util

import java.io.ByteArrayOutputStream
import androidx.core.net.toUri

class AndroidMediaContentReader : MediaContentReader {
    override suspend fun getMediaBytes(filePath: String): ByteArray {
        val contentResolver = ApplicationContext.activity?.contentResolver
            ?: throw IllegalStateException("Contexto da aplicação não disponível")

        val uri = filePath.toUri()

        return contentResolver.openInputStream(uri)?.use { inputStream ->
            ByteArrayOutputStream().apply {
                inputStream.copyTo(this)
            }.toByteArray()
        } ?: throw IllegalStateException("Não foi possível ler o arquivo: $filePath")
    }
}