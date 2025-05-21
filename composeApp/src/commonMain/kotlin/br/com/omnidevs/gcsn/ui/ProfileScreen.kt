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
import br.com.omnidevs.gcsn.model.post.Post
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import gcsn.composeapp.generated.resources.avatarMasc
import gcsn.composeapp.generated.resources.banner
import org.jetbrains.compose.resources.painterResource

data class ProfileScreen(
    val actor: Actor,
//    val feed: Feed
) : Screen {

    @Composable
    override fun Content() {
        // The existing Composable content of ProfileScreen
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeader(actor = actor)
//            LazyColumn(modifier = Modifier.fillMaxSize()) {
//                items(feed.posts.size) { index ->
//                    PostItem(post = feed.posts[index])
//                }
//            }
        }
    }

}

@Composable
fun ProfileHeader(actor: Actor) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Consider making this dynamic or more robust
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

//@Composable
//fun PostItem(post: Post) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Text(text = post.text, fontSize = 14.sp)
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = post.createdAt, // Assuming this is a formatted date string
//            fontSize = 12.sp,
//            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
//        )
//        Divider(modifier = Modifier.padding(vertical = 8.dp))
//    }
//}