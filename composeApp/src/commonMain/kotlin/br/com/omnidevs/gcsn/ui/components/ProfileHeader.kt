package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import br.com.omnidevs.gcsn.model.actor.Actor
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import gcsn.composeapp.generated.resources.avatarMasc
import gcsn.composeapp.generated.resources.banner
import org.jetbrains.compose.resources.painterResource

@Composable
fun ProfileHeader(actor: Actor) {
    val showPopup = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        // Banner image section - unchanged
        if (actor.banner?.isNotEmpty() == true) {
            AsyncImage(
                model = actor.banner.toString(),
                contentDescription = "Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
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

        // Profile info section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
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
            }

            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = actor.displayName ?: actor.handle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "@${actor.handle}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                actor.description?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileStat(label = "Seguidores", value = actor.followersCount)
                    ProfileStat(label = "Seguindo", value = actor.followsCount)
                    ProfileStat(label = "Posts", value = actor.postsCount)
                }
            }
        }

        // Edit profile text
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Editar perfil",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showPopup.value = true }
                    .padding(8.dp)
            )
        }

        // Popup dialog
        if (showPopup.value) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showPopup.value = false }
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(16.dp)
                ) {
                    Text("Função não implementada ainda", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}