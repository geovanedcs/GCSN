package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.omnidevs.gcsn.model.FeedItem
import br.com.omnidevs.gcsn.model.RepostReason
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import br.com.omnidevs.gcsn.model.post.embed.Embed
import br.com.omnidevs.gcsn.util.DateUtils
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PostItem(
    feedItem: FeedItem,
    onAuthorClick: (String) -> Unit = {},
    onLikeClick: (Post, Boolean) -> Unit = { _, _ -> },
    onParentClick: (String) -> Unit = {},
    showConfirmationDialog: ((String, String, String, ConfirmationDialogType, () -> Unit) -> Unit)? = null
) {
    val post = feedItem.post
    var isLiked by remember { mutableStateOf(post.viewer?.like != null) }
    var likeCount by remember { mutableIntStateOf(post.likeCount) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Display repost information if available
        if (feedItem.reason is RepostReason) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Filled.Repeat,
                    contentDescription = "Repostado",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Repostado por ${feedItem.reason.by.displayName ?: feedItem.reason.by.handle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { onAuthorClick(feedItem.reason.by.did) }
                )
            }
        }

        // Display reply information if available
        feedItem.reply?.let { reply ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Filled.Reply,
                    contentDescription = "Respondendo a",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                val parentPost = reply.parent
                if(parentPost is Post){
                    Text(
                        text = "Respondendo a ${parentPost.author.displayName ?: parentPost.author.handle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.clickable { onParentClick(parentPost.uri) }
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onAuthorClick(post.author.did) }
                .fillMaxWidth()) {
            AsyncImage(
                model = post.author.avatar ?: Res.getUri("drawable/avatar"),
                contentDescription = "Avatar de ${post.author.displayName ?: post.author.handle}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author.displayName ?: post.author.handle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${post.author.handle}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
                Text(
                    text = DateUtils.formatToSocialDate(post.record.createdAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (post.record.text.isNotBlank()) {
            Text(
                text = post.record.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val viewEmbed = post.embed
        if (viewEmbed is Embed.ImagesView && viewEmbed.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Se tiver mais de uma imagem, usar o carrossel
            if (viewEmbed.images.size > 1) {
                ImageCarousel(images = viewEmbed.images)
            } else {
                // Para uma única imagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = viewEmbed.images.first().thumb,
                        contentDescription = viewEmbed.images.first().alt?.ifEmpty { "Imagem do post" },
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostAction(
                icon = Icons.Filled.Reply,
                count = post.replyCount,
                contentDescription = "Replies",
                onClick = {
                    // Navigate to thread view when clicking on replies
                    onParentClick(post.uri)
                }
            )
            PostAction(
                icon = Icons.Filled.Repeat,
                count = post.repostCount,
                contentDescription = "Reposts"
            )
            PostAction(
                icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                count = likeCount,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                onClick = {
                    val wasLiked = isLiked

                    if (wasLiked && showConfirmationDialog != null) {
                        // Show confirmation dialog for unliking
                        showConfirmationDialog(
                            "Remover curtida",
                            "Tem certeza que deseja remover sua curtida desta postagem?",
                            "Remover",
                            ConfirmationDialogType.NORMAL
                        ) {
                            // This block runs when user confirms the action
                            isLiked = false
                            likeCount -= 1
                            onLikeClick(post, false)
                        }
                    } else {
                        // For liking, no confirmation needed
                        isLiked = !isLiked
                        likeCount += if (isLiked) 1 else -1
                        onLikeClick(post, !wasLiked)
                    }
                }
            )
        }
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun ImageCarousel(images: List<Embed.ImageView>) {
    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .clip(MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        // Imagem atual
        AsyncImage(
            model = images[currentPage].thumb,
            contentDescription = images[currentPage].alt?.ifEmpty { "Imagem do post" },
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )

        // Botões de navegação
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão anterior
            if (currentPage > 0) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Imagem anterior",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { currentPage = currentPage - 1 }
                        .padding(8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }

            // Botão próximo
            if (currentPage < images.size - 1) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Próxima imagem",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { currentPage = currentPage + 1 }
                        .padding(8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // Indicadores de página
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            images.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentPage == index)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun PostAction(
    icon: ImageVector,
    count: Int,
    contentDescription: String,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(),
            fontSize = 12.sp,
            color = tint
        )
    }
}