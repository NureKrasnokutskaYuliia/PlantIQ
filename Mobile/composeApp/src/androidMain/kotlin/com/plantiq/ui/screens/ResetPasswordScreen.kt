package com.plantiq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plantiq.viewmodel.ForgotPasswordViewModel
import com.plantiq.viewmodel.ResetPasswordState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    prefillCode: String = "",
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    var code by remember { mutableStateOf(prefillCode) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val resetState by viewModel.resetState.collectAsState()

    val isPasswordInvalid = newPassword.isNotEmpty() && newPassword.length < 6
    val isConfirmInvalid = confirmPassword.isNotEmpty() && newPassword != confirmPassword
    val isFormValid = code.length == 6 && newPassword.isNotEmpty() && !isPasswordInvalid && !isConfirmInvalid

    LaunchedEffect(resetState) {
        if (resetState is ResetPasswordState.Success) {
            showSuccess = true
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Пароль змінено!") },
            text = { Text("Ваш пароль успішно оновлено. Тепер ви можете увійти з новим паролем.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetResetState()
                    onSuccess()
                }) {
                    Text("Увійти")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новий пароль") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Код надіслано на:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = email,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text("6-значний код") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = code.isNotEmpty() && code.length < 6,
                supportingText = {
                    if (code.isNotEmpty() && code.length < 6) {
                        Text("Код складається з 6 цифр", color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Новий пароль") },
                singleLine = true,
                isError = isPasswordInvalid,
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (newPasswordVisible) "Сховати пароль" else "Показати пароль"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (isPasswordInvalid) Text("Мінімум 6 символів", color = MaterialTheme.colorScheme.error)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Підтвердіть пароль") },
                singleLine = true,
                isError = isConfirmInvalid,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Сховати пароль" else "Показати пароль"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (isConfirmInvalid) Text("Паролі не співпадають", color = MaterialTheme.colorScheme.error)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (resetState is ResetPasswordState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.resetPassword(email, code, newPassword, confirmPassword) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = isFormValid
                ) {
                    Text("Змінити пароль")
                }
            }

            if (resetState is ResetPasswordState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (resetState as ResetPasswordState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
