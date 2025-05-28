 package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

@Composable
fun RegisterUserScreen(api: BlueskyApi) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = handle,
            onValueChange = { handle = it },
            label = { Text("Handle") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = inviteCode,
            onValueChange = { inviteCode = it },
            label = { Text("Código de Convite (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
//                CoroutineScope(Dispatchers.IO).launch {
//                    try {
//                        val response = api.registerAccount(email, password, handle, inviteCode.ifEmpty { null })
//                        result = if (response.success) "Usuário criado com sucesso!" else "Erro: ${response.error}"
//                    } catch (e: Exception) {
//                        result = "Erro: ${e.message}"
//                    }
//                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Usuário")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(result)
    }
}