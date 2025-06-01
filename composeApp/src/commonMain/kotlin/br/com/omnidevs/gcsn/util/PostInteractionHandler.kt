package br.com.omnidevs.gcsn.util

import androidx.compose.material3.SnackbarHostState
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import br.com.omnidevs.gcsn.model.post.Viewer
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.ConfirmationDialogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object PostInteractionHandler {

    fun handleLikeAction(
        post: Post,
        isLiking: Boolean,
        api: BlueskyApi,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        showDialog: (String, String, String, ConfirmationDialogType, () -> Unit) -> Unit,
        onActionComplete: ((Post) -> Unit)? = null
    ) {
        if (!isLiking) {
            showDialog(
                "Remover curtida",
                "Tem certeza que deseja remover sua curtida desta postagem?",
                "Remover",
                ConfirmationDialogType.NORMAL,
                { performLikeAction(post, false, api, scope, snackbarHostState, onActionComplete) }
            )
        } else {
            performLikeAction(post, true, api, scope, snackbarHostState, onActionComplete)
        }
    }

    private fun performLikeAction(
        post: Post,
        isLiking: Boolean,
        api: BlueskyApi,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        onActionComplete: ((Post) -> Unit)? = null
    ) {
        scope.launch {
            try {
                println("DEBUG: performLikeAction - isLiking=$isLiking, postURI=${post.uri}")

                if (isLiking) {
                    val response = api.likePost(post.uri, post.cid)
                    println("DEBUG: Like successful with URI: ${response.uri}")

                    val updatedPost = post.copy(
                        viewer = post.viewer?.copy(like = response.uri)
                            ?: br.com.omnidevs.gcsn.model.post.Viewer(like = response.uri)
                    )
                    onActionComplete?.invoke(updatedPost)
                } else {
                    post.viewer?.like?.let { likeUri ->
                        println("DEBUG: Attempting to unlike with URI: $likeUri")
                        val result = api.unlikePost(likeUri)
                        println("DEBUG: Unlike API result: $result")

                        // Update post state regardless of API result
                        val updatedPost = post.copy(
                            viewer = post.viewer.copy(like = null)
                        )
                        onActionComplete?.invoke(updatedPost)
                    } ?: println("DEBUG: No like URI found to unlike")
                }
            } catch (e: Exception) {
                println("DEBUG: Like error: ${e.message}")
                e.printStackTrace()
                snackbarHostState.showSnackbar(
                    if (isLiking) "Falha ao curtir: ${e.message}"
                    else "Falha ao descurtir: ${e.message}"
                )
            }
        }
    }

    fun handleRepostAction(
        post: Post,
        isReposting: Boolean,
        api: BlueskyApi,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        showDialog: (String, String, String, ConfirmationDialogType, () -> Unit) -> Unit,
        onActionComplete: ((Post) -> Unit)? = null
    ) {
        if (!isReposting) {
            showDialog(
                "Remover repost",
                "Tem certeza que deseja remover seu repost desta postagem?",
                "Remover",
                ConfirmationDialogType.NORMAL,
                {
                    performRepostAction(
                        post,
                        false,
                        api,
                        scope,
                        snackbarHostState,
                        onActionComplete
                    )
                }
            )
        } else {
            performRepostAction(post, true, api, scope, snackbarHostState, onActionComplete)
        }
    }

    private fun performRepostAction(
        post: Post,
        isReposting: Boolean,
        api: BlueskyApi,
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        onActionComplete: ((Post) -> Unit)? = null
    ) {
        scope.launch {
            try {
                println("DEBUG: performRepostAction - isReposting=$isReposting, postURI=${post.uri}")

                if (isReposting) {
                    val response = api.repostPost(post.uri, post.cid)
                    println("DEBUG: Repost successful with URI: ${response.uri}")

                    val updatedPost = post.copy(
                        viewer = post.viewer?.copy(repost = response.uri)
                            ?: Viewer(repost = response.uri)
                    )
                    onActionComplete?.invoke(updatedPost)
                } else {
                    post.viewer?.repost?.let { repostUri ->
                        println("DEBUG: Attempting to delete repost with URI: $repostUri")
                        val result = api.deleteRepost(repostUri)
                        println("DEBUG: Delete repost API result: $result")

                        // Update post state regardless of API result
                        val updatedPost = post.copy(
                            viewer = post.viewer.copy(repost = null)
                        )
                        onActionComplete?.invoke(updatedPost)
                    } ?: println("DEBUG: No repost URI found to delete")
                }
            } catch (e: Exception) {
                println("DEBUG: Repost error: ${e.message}")
                e.printStackTrace()
                snackbarHostState.showSnackbar(
                    if (isReposting) "Falha ao repostar: ${e.message}"
                    else "Falha ao remover repost: ${e.message}"
                )
            }
        }
    }
}