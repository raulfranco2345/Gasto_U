package com.example.gastou.presentation.auth

import com.example.gastou.core.ui.components.*
import com.example.gastou.domain.model.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gastou.core.ui.components.GastoUButton
import com.example.gastou.core.ui.components.GastoULogo
import com.example.gastou.core.ui.components.GastoUTextField
import com.example.gastou.core.ui.theme.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        GastoULogo(fontSize = 24, modifier = Modifier.offset(x = (-24).dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Placeholder for small illustration
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GastoUBackgroundLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = GastoUGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Crea tu cuenta",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GastoUTextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Empieza a administrar tu dinero inteligente\ny alcanza tus metas universitarias",
                    fontSize = 14.sp,
                    color = GastoUTextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Form Fields
                FormFieldLabel("Nombre completo")
                GastoUTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    placeholder = "Tu nombre y apellido",
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = GastoULightGreen) }
                )
                FieldError(uiState.nameError)
                
                Spacer(modifier = Modifier.height(16.dp))

                FormFieldLabel("Correo institucional o personal")
                GastoUTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = "ejemplo@correo.edu / personal",
                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = GastoULightGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                FieldError(uiState.emailError)

                Spacer(modifier = Modifier.height(16.dp))

                FormFieldLabel("Contraseña")
                GastoUTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Crea una contraseña segura",
                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = GastoULightGreen) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                )
                FieldError(uiState.passwordError)

                Spacer(modifier = Modifier.height(16.dp))

                FormFieldLabel("Confirmar contraseña")
                GastoUTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmChange,
                    placeholder = "Repite tu contraseña",
                    leadingIcon = { Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = GastoULightGreen) },
                    visualTransformation = PasswordVisualTransformation()
                )
                FieldError(uiState.confirmError)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = uiState.acceptedTerms,
                        onCheckedChange = onTermsChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = GastoUGreen,
                            uncheckedColor = Color.Gray
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    
                    Text(
                        text = buildAnnotatedString {
                            append("Acepto los ")
                            withStyle(style = SpanStyle(color = GastoUGreen, fontWeight = FontWeight.Bold)) {
                                append("Términos y Condiciones")
                            }
                            append(" y la ")
                            withStyle(style = SpanStyle(color = GastoUGreen, fontWeight = FontWeight.Bold)) {
                                append("Política de Privacidad")
                            }
                            append(" de GastoU.")
                        },
                        fontSize = 12.sp,
                        color = GastoUTextGray,
                        modifier = Modifier.padding(top = 14.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                FieldError(uiState.termsError)

                Spacer(modifier = Modifier.height(16.dp))

                ErrorBanner(uiState.errorMessage)

                Spacer(modifier = Modifier.height(8.dp))

                GastoUButton(
                    text = if (uiState.isLoading) "Creando cuenta..." else "Crear cuenta →",
                    onClick = onRegister,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = GastoUTextGray)) {
                            append("¿Ya tienes una cuenta? ")
                        }
                        withStyle(style = SpanStyle(color = GastoUGreen, fontWeight = FontWeight.Bold)) {
                            append("Iniciar sesión")
                        }
                    },
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FormFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = GastoUTextDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    GastoUTheme {
        RegisterScreen(
            uiState = RegisterUiState(),
            onNameChange = {}, onEmailChange = {}, onPasswordChange = {}, onConfirmChange = {},
            onTermsChange = {}, onRegister = {}, onNavigateBack = {}, onNavigateToLogin = {}
        )
    }
}
