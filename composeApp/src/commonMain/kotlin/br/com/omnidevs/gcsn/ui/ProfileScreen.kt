package br.com.omnidevs.gcsn.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.actor.AuthorViewer
import br.com.omnidevs.gcsn.model.post.AspectRatio
import br.com.omnidevs.gcsn.model.post.Embed
import br.com.omnidevs.gcsn.model.post.Image
import br.com.omnidevs.gcsn.model.post.Post
import br.com.omnidevs.gcsn.model.post.Record
import br.com.omnidevs.gcsn.model.post.Viewer
import br.com.omnidevs.gcsn.ui.components.PostItem
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import gcsn.composeapp.generated.resources.avatarMasc
import gcsn.composeapp.generated.resources.banner
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

data class ProfileScreen(
    val actor: Actor,
    val feed: Feed
) : Screen {

    @Composable
    override fun Content() {
        // The existing Composable content of ProfileScreen
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(actor = actor)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(feed.posts.size) { index ->
                    PostItem(post = feed.posts[index])
                }
            }
        }
    }

}

@Composable
fun ProfileHeader(actor: Actor) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Banner
        if (actor.banner?.isNotEmpty() == true) {
            AsyncImage(
                model = actor.banner.toString(),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.banner),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }

        // Avatar e informações
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                // Adjusted padding to ensure avatar overlaps banner nicely
                .padding(top = 100.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (actor.avatar?.isNotEmpty() == true) {
                AsyncImage(
                    model = actor.avatar.toString(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(Res.drawable.avatarMasc),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = actor.displayName ?: actor.handle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
            Text(text = "@${actor.handle}", fontSize = 16.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            actor.description?.let {
                Text(text = it, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp)) // Added space before stats for better layout

            // Estatísticas do perfil
            Row(
                horizontalArrangement = Arrangement.SpaceAround, // SpaceAround for better distribution
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileStat(label = "Seguidores", value = actor.followersCount)
                ProfileStat(label = "Seguindo", value = actor.followsCount)
                ProfileStat(label = "Posts", value = actor.postsCount)
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
    }
}

val previewFeed = Feed(
    posts = listOf(
        Post(
            uri = "at://did:plc:byxe4z7g6lxdtlwwfkklr7a7/app.bsky.feed.post/3lppluev6fs2k",
            cid = "bafyreie675biobjnybbrocouduwrzrthnm4flk3hicoqayfj6nvuksl6oy",
            author = Author(
                did = "did:plc:byxe4z7g6lxdtlwwfkklr7a7",
                handle = "tcc-gcsn.bsky.social",
                displayName = "",
                avatar = "https://cdn.bsky.app/img/avatar/plain/did:plc:byxe4z7g6lxdtlwwfkklr7a7/bafkreifiqsw6lon2ysterag3ktcvq6tc2yr242vsbfwrmtccqxo2khwypq@jpeg",
                viewer = AuthorViewer(
                    muted = false,
                    blockedBy = false
                ),
                labels = emptyList(),
                createdAt = "2025-05-19T22:18:40.340Z"
            ),
            record = Record(
                type = "app.bsky.feed.post",
                text = "Testando post",
                createdAt = "2025-05-21T21:57:48.426Z",
                langs = listOf("pt")
            ),
            embed = Embed.ImagesView(
                images = listOf(
                    Image(
                        thumb = "https://cdn.bsky.app/img/feed_thumbnail/plain/did:plc:byxe4z7g6lxdtlwwfkklr7a7/bafkreigrihutgmynjyvm3cld5pg6zxey277wwv4ihihjotr76kmjytc5o4@jpeg",
                        fullsize = "https://cdn.bsky.app/img/feed_fullsize/plain/did:plc:byxe4z7g6lxdtlwwfkklr7a7/bafkreigrihutgmynjyvm3cld5pg6zxey277wwv4ihihjotr76kmjytc5o4@jpeg",
                        alt = "",
                        aspectRatio = AspectRatio(
                            height = 1024,
                            width = 1024
                        )
                    )
                )
            ),
            replyCount = 0,
            repostCount = 0,
            likeCount = 0,
            quoteCount = 0,
            indexedAt = "2025-05-21T21:57:55.151Z",
            viewer = Viewer(
                threadMuted = false,
                embeddingDisabled = false
            ),
            labels = emptyList()
        )
    )
)
val actor = Actor(
    did = "did:plc:byxe4z7g6lxdtlwwfkklr7a7",
    handle = "tcc-gcsn.bsky.social",
    displayName = "",
    avatar = "https://cdn.bsky.app/img/avatar/plain/did:plc:byxe4z7g6lxdtlwwfkklr7a7/bafkreifiqsw6lon2ysterag3ktcvq6tc2yr242vsbfwrmtccqxo2khwypq@jpeg",
    indexedAt = "2025-05-19T22:18:40.340Z",
    followersCount = 1,
    followsCount = 1,
    postsCount = 1
)
@Preview
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(
        actor = actor,
        feed = previewFeed
    )
}