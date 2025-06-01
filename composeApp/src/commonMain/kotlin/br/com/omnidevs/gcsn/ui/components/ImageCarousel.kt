package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.post.embed.ImageView
import coil3.compose.AsyncImage

@Composable
fun ImageCarousel(images: List<ImageView>) {
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