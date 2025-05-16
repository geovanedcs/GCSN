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
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.post.Post
import gcsn.composeapp.generated.resources.Res
import gcsn.composeapp.generated.resources.avatarMasc
import gcsn.composeapp.generated.resources.banner
import gcsn.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileScreen(
    actor: Actor,
    posts: List<Post>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Cabeçalho do perfil
        ProfileHeader(actor = actor)

        // Lista de posts
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(posts.size) { index ->
                PostItem(post = posts[index])
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
        if(actor.banner != null) {
            Image(
                painter = painterResource(Res.drawable.banner), // Substitua pelo recurso correto do banner
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.banner), // Substitua pelo recurso correto do logo
                contentDescription = "Logo",
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
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.avatarMasc), // Substitua pelo recurso correto do avatar
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = actor.displayName ?: actor.handle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "@${actor.handle}", fontSize = 16.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            actor.description?.let {
                Text(text = it, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Estatísticas do perfil
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
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

@Composable
fun PostItem(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = post.text, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = post.createdAt,
            fontSize = 12.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
