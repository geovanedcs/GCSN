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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.omnidevs.gcsn.model.FeedItem
import br.com.omnidevs.gcsn.model.RepostReason
import br.com.omnidevs.gcsn.model.post.Facet
import br.com.omnidevs.gcsn.model.post.FacetFeature
import br.com.omnidevs.gcsn.model.post.LinkFeature
import br.com.omnidevs.gcsn.model.post.MentionFeature
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import br.com.omnidevs.gcsn.model.post.TagFeature
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
    onLikeClick: (Post, Boolean, (Post) -> Unit) -> Unit = { _, _, _ -> },
    onRepostClick: (Post, Boolean, (Post) -> Unit) -> Unit = { _, _, _ -> },
    onParentClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
    showConfirmationDialog: ((String, String, String, ConfirmationDialogType, () -> Unit) -> Unit)? = null
) {
    val post = feedItem.post
    // Use state variables to track UI state locally with initial values from the post
    var isLiked by remember { mutableStateOf(post.viewer?.like != null) }
    var likeCount by remember { mutableIntStateOf(post.likeCount) }
    var isReposted by remember { mutableStateOf(post.viewer?.repost != null) }
    var repostCount by remember { mutableIntStateOf(post.repostCount) }

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
                if (parentPost is Post) {
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
            if (post.record.facets.isNullOrEmpty()) {
                // Regular text without facets
                Text(
                    text = post.record.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Text with facets (mentions, hashtags, links)
                val annotatedText = createFacetText(
                    text = post.record.text,
                    facets = post.record.facets
                )

                FacetText(
                    annotatedText = annotatedText,
                    onTagClick = onTagClick,
                    onMentionClick = onMentionClick,
                    onLinkClick = onLinkClick
                )
            }
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
        } else if (viewEmbed is Embed.ExternalView) {
            Spacer(modifier = Modifier.height(8.dp))

            ExternalLinkEmbed(
                uri = viewEmbed.external.uri,
                title = viewEmbed.external.title,
                description = viewEmbed.external.description,
                thumbUrl = viewEmbed.external.thumb,
                onClick = {
                    onLinkClick(viewEmbed.external.uri)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else if (viewEmbed != null) {
        // Handle unknown or unsupported embed types
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Este recurso não está disponível no momento.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
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
                    onParentClick(post.uri)
                }
            )
            PostAction(
                icon = Icons.Filled.Repeat,
                count = repostCount,
                contentDescription = if (isReposted) "Remover repost" else "Repostar",
                tint = if (isReposted)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                onClick = {
                    // First determine if we're reposting or unreposting
                    val isReposting = !isReposted

                    // Pass a callback to update this component's state after API action
                    onRepostClick(post, isReposting) { updatedPost ->
                        // Update our local state with the API result
                        isReposted = updatedPost.viewer?.repost != null
                        repostCount = updatedPost.repostCount
                    }
                }
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
                    // First determine if we're liking or unliking
                    val isLiking = !isLiked

                    // Pass a callback to update this component's state after API action
                    onLikeClick(post, isLiking) { updatedPost ->
                        // Update our local state with the API result
                        isLiked = updatedPost.viewer?.like != null
                        likeCount = updatedPost.likeCount
                    }
                }
            )
        }
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun createFacetText(
    text: String,
    facets: List<Facet>
): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // Apply styling for all facets
        for (facet in facets) {
            val start = facet.index.byteStart
            val end = facet.index.byteEnd

            // Skip invalid facet indices
            if (start < 0 || end > text.length || start >= end) continue

            for (feature in facet.features) {
                when (feature) {
                    is TagFeature -> {
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            start = start,
                            end = end
                        )
                        addStringAnnotation(
                            tag = "tag",
                            annotation = feature.tag,
                            start = start,
                            end = end
                        )
                    }
                    is MentionFeature -> {
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            start = start,
                            end = end
                        )
                        addStringAnnotation(
                            tag = "mention",
                            annotation = feature.did,
                            start = start,
                            end = end
                        )
                    }
                    is LinkFeature -> {
                        addStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline
                            ),
                            start = start,
                            end = end
                        )
                        addStringAnnotation(
                            tag = "link",
                            annotation = feature.uri,
                            start = start,
                            end = end
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FacetText(
    annotatedText: AnnotatedString,
    onTagClick: (String) -> Unit,
    onMentionClick: (String) -> Unit,
    onLinkClick: (String) -> Unit
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )

    androidx.compose.foundation.text.ClickableText(
        text = annotatedText,
        style = textStyle,
        onClick = { offset ->
            // Check for tag annotations at clicked position
            annotatedText.getStringAnnotations(tag = "tag", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onTagClick(annotation.item)
                    return@ClickableText
                }

            // Check for mention annotations
            annotatedText.getStringAnnotations(tag = "mention", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onMentionClick(annotation.item)
                    return@ClickableText
                }

            // Check for link annotations
            annotatedText.getStringAnnotations(tag = "link", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onLinkClick(annotation.item)
                    return@ClickableText
                }
        }
    )
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