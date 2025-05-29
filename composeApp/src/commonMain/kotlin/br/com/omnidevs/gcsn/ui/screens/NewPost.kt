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
import br.com.omnidevs.gcsn.model.ImageFile
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
// import dev.icerock.moko.media.compose.rememberMediaPickerControllerFactory // Removido
import dev.icerock.moko.media.picker.MediaPickerController
// import dev.icerock.moko.media.MediaType // Removido se não usado
import dev.icerock.moko.media.FileMedia
// import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory // Removido
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.launch

class CreatePostScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val blueskyApi = remember { BlueskyApi() }
        var postText by remember { mutableStateOf("") }
        var isPosting by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val userData = remember { AppDependencies.authService.getUserData() }
        var userAvatar by remember { mutableStateOf<String?>(null) }

        // Obtenha os controladores de AppDependencies
        val mediaPickerController = remember { AppDependencies.mediaPickerController }
        val permissionsController = remember { AppDependencies.permissionsController }

        var selectedImages by remember { mutableStateOf<List<ImageFile>>(emptyList()) }

        val MAX_IMAGES = 4

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
                                        selectedImages =
                                            selectedImages.filterNot { it == imageFile }
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
                                coroutineScope.launch {
                                    pickImageAndUpdateList(
                                        mediaPickerController = mediaPickerController, // Usando a instância de AppDependencies
                                        permissionsController = permissionsController, // Usando a instância de AppDependencies
                                        currentImages = selectedImages,
                                        onImagesUpdated = { newImages ->
                                            selectedImages = newImages
                                        },
                                        onError = { errorMsg ->
                                            errorMessage = errorMsg
                                        }
                                    )
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

    private suspend fun pickImageAndUpdateList(
        mediaPickerController: MediaPickerController,
        permissionsController: PermissionsController,
        currentImages: List<ImageFile>,
        onImagesUpdated: (List<ImageFile>) -> Unit,
        onError: (String) -> Unit
    ) {
        println("MokoTest: pickImageAndUpdateList called")
        try {
            println("MokoTest: --- About to call isPermissionGranted(Permission.GALLERY) ---")
            val isGranted = permissionsController.isPermissionGranted(Permission.GALLERY)
            println("MokoTest: --- isPermissionGranted(Permission.GALLERY) returned: $isGranted ---")

            if (!isGranted) {
                println("MokoTest: Permission.GALLERY not granted. --- About to call providePermission(Permission.GALLERY) ---")
                permissionsController.providePermission(Permission.GALLERY)
                println("MokoTest: --- providePermission(Permission.GALLERY) finished. --- Checking permission again.")
                val isGrantedAfterRequest =
                    permissionsController.isPermissionGranted(Permission.GALLERY)
                println("MokoTest: --- isPermissionGranted(Permission.GALLERY) after request returned: $isGrantedAfterRequest ---")
                if (!isGrantedAfterRequest) {
                    println("MokoTest: Permission.GALLERY still not granted after request.")
                    onError("Permissão para acessar a galeria não foi concedida.")
                    return
                }
                println("MokoTest: Permission.GALLERY granted after request.")
            } else {
                println("MokoTest: Permission.GALLERY already granted.")
            }

            println("MokoTest: About to call mediaPickerController.pickFiles()")
            val fileMedia: FileMedia = mediaPickerController.pickFiles()
            println("MokoTest: mediaPickerController.pickFiles() returned: ${fileMedia.name}, Path: ${fileMedia.path}")

            val filePath = fileMedia.path
            val fileName = fileMedia.name

            val extension = fileName.substringAfterLast('.', "").lowercase()
            val inferredMimeType = when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                else -> null
            }
            println("MokoTest: File: $fileName, Extension: $extension, MimeType: $inferredMimeType")

            if (inferredMimeType?.startsWith("image/") == true) {
                val fileSize = 0L // O tamanho ainda precisa ser obtido por outros meios
                val imageFile = ImageFile(
                    uri = filePath,
                    name = fileName,
                    mimeType = inferredMimeType,
                    size = fileSize
                )
                onImagesUpdated(currentImages + imageFile)
                println("MokoTest: Image processed and updated.")
            } else {
                println("MokoTest: Not an image file or unknown type. Extension: .$extension, MimeType: $inferredMimeType")
                onError("Por favor, selecione um arquivo de imagem. Extensão detectada: .$extension (Tipo MIME inferido: ${inferredMimeType ?: "desconhecido"})")
            }
        } catch (e: DeniedException) {
            println("MokoTest: DeniedException: ${e.message}")
            e.printStackTrace()
            onError("Permissão para acessar a galeria foi negada.")
        } catch (e: DeniedAlwaysException) {
            println("MokoTest: DeniedAlwaysException: ${e.message}")
            e.printStackTrace()
            onError("Permissão para acessar a galeria foi negada permanentemente. Por favor, habilite nas configurações do app.")
        } catch (e: Exception) {
            println("MokoTest: Generic Exception in pickImageAndUpdateList: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            if (e.message?.contains("cancel", ignoreCase = true) == true ||
                e::class.simpleName?.contains("Cancel", ignoreCase = true) == true ||
                e.message?.contains("No file chosen", ignoreCase = true) == true ||
                e.message?.contains("User cancelled", ignoreCase = true) == true
            ) {
                println("MokoTest: User cancelled picker or no file chosen.")
            } else {
                onError("Erro ao selecionar imagem: ${e.message ?: "Desconhecido"}")
            }
        }
    }
}