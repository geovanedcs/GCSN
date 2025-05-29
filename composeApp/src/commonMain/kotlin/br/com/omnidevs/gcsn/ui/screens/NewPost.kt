package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.getImagePickerManager
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch

class CreatePostScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val blueskyApi = remember { BlueskyApi() }
        var postText by remember { mutableStateOf("") }
        var isPosting by remember { mutableStateOf(false) }
        val imagePickerManager = getImagePickerManager()
        val selectedImages by imagePickerManager.selectedImages.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val userData = remember { AppDependencies.authService.getUserData() }
        var userAvatar by remember { mutableStateOf<String?>(null) }
        var loadingImages by remember { mutableStateOf(setOf<String>()) }
        var failedImages by remember { mutableStateOf(setOf<String>()) }

        LaunchedEffect(selectedImages) {
            if (selectedImages.isEmpty()) {
                errorMessage = null
            } else {
                println("Selected images count: ${selectedImages.size}")
                selectedImages.forEach { image ->
                    println("Image URI: ${image.uri}, Size: ${image.size}")
                }
            }
        }

        LaunchedEffect(Unit) {
            try {
                val profile = blueskyApi.getProfile(userData!!.did)
                userAvatar = profile.avatar
            } catch (e: Exception) {
                errorMessage = "Erro ao buscar avatar: ${e.message}"
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Top)
                    ),
                    title = { Text("Novo Post") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                isPosting = true
                                coroutineScope.launch {
                                    try {
                                        val imageUris = selectedImages.map { it.uri }
                                        blueskyApi.createPost(
                                            text = postText,
                                            images = imageUris
                                        )
                                        navigator?.pop()
                                    } catch (e: Exception) {
                                        isPosting = false
                                        errorMessage = "Erro ao publicar: ${e.message}"
                                    }
                                }
                            },
                            enabled = postText.isNotEmpty() && !isPosting
                        ) {
                            Text("Publicar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        if (userAvatar != null) {
                            AsyncImage(
                                model = userAvatar,
                                contentDescription = "Avatar do usuário",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text("O que está acontecendo?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        maxLines = 10
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedImages) { imageFile ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                // Track loading state with separate state variables
                                var isLoading by remember { mutableStateOf(true) }
                                var isError by remember { mutableStateOf(false) }

                                AsyncImage(
                                    model = imageFile.uri,
                                    contentDescription = "Imagem selecionada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onLoading = { isLoading = true },
                                    onSuccess = {
                                        isLoading = false
                                        isError = false
                                    },
                                    onError = {
                                        isLoading = false
                                        isError = true
                                        println("Error loading image: ${imageFile.uri}")
                                    }
                                )

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center),
                                        strokeWidth = 2.dp
                                    )
                                }

                                if (isError) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Erro ao carregar imagem",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(36.dp)
                                    )
                                }

                                // File size indicator
                                Text(
                                    text = "${imageFile.size / 1024} KB",
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(4.dp)
                                )

                                // Remove button
                                IconButton(
                                    onClick = { imagePickerManager.removeImage(imageFile) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remover imagem",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            errorMessage = null
                            coroutineScope.launch {
                                try {
                                    imagePickerManager.pickSingleImage()
                                } catch (e: Exception) {
                                    errorMessage = "Erro ao selecionar imagens: ${e.message}"
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Adicionar imagem")
                    }
                }
            }

            if (isPosting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}