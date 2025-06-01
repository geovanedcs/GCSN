package br.com.omnidevs.gcsn.model.actor

import br.com.omnidevs.gcsn.model.Label
import kotlinx.serialization.Serializable


@Serializable
data class Author(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val viewer: AuthorViewer? = null,
    val labels: List<Label> = emptyList(),
    val associated: Associated? = null,
    val verification: Verification? = null,
    val createdAt: String
)

@Serializable
data class Verification(
    val verifications: List<VerificationEntry> = emptyList(),
    val verifiedStatus: String,
    val trustedVerifierStatus: String
)

@Serializable
data class VerificationEntry(
    val issuer: String,
    val uri: String,
    val isValid: Boolean,
    val createdAt: String
)