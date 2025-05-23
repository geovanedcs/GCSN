package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.omnidevs.gcsn.model.post.embed.Embed
import br.com.omnidevs.gcsn.model.post.Post
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.text.isNotBlank

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PostItem(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = post.author.avatar ?: Res.getUri("drawable/avatar"),
                contentDescription = "Avatar de ${post.author.displayName ?: post.author.handle}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
                    .clickable { },
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = post.author.displayName ?: post.author.handle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "@${post.author.handle}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (post.record.text.isNotBlank()) {
            Text(
                text = post.record.text,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        val viewEmbed = post.embed
        if(viewEmbed is Embed.ImageView) {
            Spacer(modifier = Modifier.height(8.dp))
            viewEmbed.images?.firstOrNull().let{ viewImage ->
                AsyncImage(
                    model = viewImage?.thumb,
                    contentDescription = viewImage?.alt?.ifEmpty { "Imagem do post" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(
            text = post.record.createdAt,
            fontSize = 12.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostAction(
                icon = Icons.Filled.FavoriteBorder,
                count = post.replyCount,
                contentDescription = "Replies"
            )
            PostAction(
                icon = Icons.Filled.FavoriteBorder,
                count = post.repostCount,
                contentDescription = "Reposts"
            )
            PostAction(
                icon = Icons.Filled.FavoriteBorder,
                count = post.likeCount,
                contentDescription = "Likes"
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}


@Composable
fun PostAction(icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int, contentDescription: String, tint: Color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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