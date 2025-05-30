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
import br.com.omnidevs.gcsn.model.ImageFile
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import com.mohamedrejeb.calf.io.name
import com.mohamedrejeb.calf.io.path
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.PermissionStatus
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi::class)
class CreatePostScreen : Screen {
    private val MAX_IMAGES = 4

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val blueskyApi = remember { BlueskyApi() }
        val coroutineScope = rememberCoroutineScope()
        var postText by remember { mutableStateOf("") }
        var selectedImages by remember { mutableStateOf<List<ImageFile>>(emptyList()) }
        var isPosting by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val userData = remember { AppDependencies.authService.getUserData() }
        var userAvatar by remember { mutableStateOf<String?>(null) }

        // Setup permission state
        val galleryPermissionState = rememberPermissionState(Permission.ReadStorage)

        // Setup file picker launcher
        val filePickerLauncher = rememberFilePickerLauncher(
            type = FilePickerFileType.Image,
            selectionMode = FilePickerSelectionMode.Single,
            onResult = { files ->
                coroutineScope.launch {
                    try {
                        if (files.isNotEmpty()) {
                            val file = files.first()
                            val fileName = file.name
                            val filePath = file.path?.toString() ?: ""

                            // Read bytes directly with Calf and cache them
                            val fileBytes = try {
                                file.readByteArray().also { bytes ->
                                    // Cache the bytes for later use during upload
                                    BlueskyApi.cacheImageBytes(filePath, bytes)
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erro ao ler o arquivo: ${e.message}"
                                null
                            }

                            val fileSize = fileBytes?.size?.toLong() ?: 1024L

                            val extension = fileName?.substringAfterLast('.', "")?.lowercase() ?: ""
                            val inferredMimeType = when (extension) {
                                "jpg", "jpeg" -> "image/jpeg"
                                "png" -> "image/png"
                                "gif" -> "image/gif"
                                "webp" -> "image/webp"
                                else -> null
                            }

                            if (inferredMimeType?.startsWith("image/") == true) {
                                val imageFile = ImageFile(
                                    uri = filePath,
                                    name = fileName?.toString() ?: "unknown",
                                    mimeType = inferredMimeType,
                                    size = fileSize
                                )

                                if (selectedImages.size < MAX_IMAGES) {
                                    selectedImages = selectedImages + imageFile
                                } else {
                                    errorMessage = "Máximo de $MAX_IMAGES imagens permitidas."
                                }
                            } else {
                                errorMessage = "Por favor, selecione um arquivo de imagem válido."
                            }
                        }
                    } catch (e: Exception) {
                        // Only show error for non-cancellation exceptions
                        if (e.message?.contains("cancel", ignoreCase = true) != true) {
                            errorMessage = "Erro ao selecionar imagem: ${e.message ?: "Desconhecido"}"
                        }
                    }
                }
            }
        )

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
                                if (postText.isEmpty() && selectedImages.isEmpty()) {
                                    errorMessage = "Por favor, adicione texto ou imagens à sua postagem."
                                    return@Button
                                }

                                isPosting = true
                                coroutineScope.launch {
                                    try {
                                        blueskyApi.createPost(
                                            text = postText,
                                            images = selectedImages
                                        )
                                        navigator?.pop()
                                    } catch (e: Exception) {
                                        isPosting = false
                                        errorMessage = "Erro ao publicar: ${e.message}"
                                    }
                                }
                            },
                            enabled = !isPosting && (postText.isNotEmpty() || selectedImages.isNotEmpty())
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

                                Text(
                                    text = "${imageFile.size / 1024} KB",
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filterNot { it == imageFile }
                                    },
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
                            if (selectedImages.size >= MAX_IMAGES) {
                                errorMessage = "Máximo de $MAX_IMAGES imagens permitidas."
                            } else {
                                errorMessage = null

                                // Check permission before launching picker
                                when {
                                    galleryPermissionState.status == PermissionStatus.Granted -> {
                                        filePickerLauncher.launch()
                                    }

                                    else -> {
                                        coroutineScope.launch {
                                            galleryPermissionState.launchPermissionRequest()
                                            if (galleryPermissionState.status == PermissionStatus.Granted) {
                                                filePickerLauncher.launch()
                                            } else {
                                                errorMessage =
                                                    "Permissão para acessar a galeria não foi concedida."
                                            }
                                        }
                                    }
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