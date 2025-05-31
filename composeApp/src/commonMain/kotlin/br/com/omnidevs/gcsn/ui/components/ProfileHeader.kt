package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.actor.Actor
import coil3.compose.AsyncImage

@Composable
fun ProfileHeader(
    actor: Actor,
    isOwnProfile: Boolean = false,
    isLoading: Boolean = false,
    onFollowClick: (Boolean) -> Unit = {},
    showConfirmationDialog: ((String, String, String, ConfirmationDialogType, () -> Unit) -> Unit)? = null
) {
    val isFollowing = actor.viewer?.following != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Banner image (if available)
        actor.banner?.let { banner ->
            AsyncImage(
                model = banner,
                contentDescription = "Banner de ${actor.displayName ?: actor.handle}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Avatar and profile info
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            AsyncImage(
                model = actor.avatar,
                contentDescription = "Avatar de ${actor.displayName ?: actor.handle}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Display name and handle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = actor.displayName ?: actor.handle,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "@${actor.handle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Follow/follower counts
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${actor.followsCount} seguindo",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${actor.followersCount} seguidores",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Description
                actor.description?.let { description ->
                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Follow button (only show if not viewing own profile)
            if (!isOwnProfile) {
                Spacer(modifier = Modifier.width(8.dp))
                FollowButton(
                    isFollowing = isFollowing,
                    isLoading = isLoading,
                    onClick = {
                        if (isFollowing && showConfirmationDialog != null) {
                            // Show confirmation dialog when unfollowing
                            val displayName = actor.displayName ?: actor.handle
                            showConfirmationDialog(
                                "Deixar de seguir",
                                "Tem certeza que deseja deixar de seguir @$displayName?",
                                "Deixar de seguir",
                                ConfirmationDialogType.WARNING
                            ) {
                                // This executes when the user confirms
                                onFollowClick(false)
                            }
                        } else {
                            // Follow directly without confirmation
                            onFollowClick(!isFollowing)
                        }
                    }
                )
            }
        }
    }
}