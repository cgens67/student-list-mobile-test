package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AuthState
import com.example.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val curLang by viewModel.currentLanguage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    // Error handling message
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            localError = (authState as AuthState.Error).message
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFe0c3fc),
                        Color(0xFF8ec5fc),
                        Color(0xFF4facfe)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Blur card container representing glassmorphism
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.White.copy(alpha = 0.2f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Language Switches Row
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.1f), CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape),
                    horizontalArrangement = Arrangement.Center
                ) {
                    listOf("en" to "EN", "bm" to "BM", "cn" to "CN").forEach { (code, label) ->
                        val active = (curLang == code)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.Transparent)
                                .clickable { viewModel.changeLanguage(code) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.Black else Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Icon block representing security lock/signup states
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF007AFF).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSignUp) Icons.Default.PersonAdd else Icons.Default.Lock,
                        contentDescription = "Authenticator Logo",
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = LanguageText.getText(curLang, if (isSignUp) "authTitleS" else "authTitleL"),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(4.dp))

                Text(
                    text = LanguageText.getText(curLang, if (isSignUp) "authSubS" else "authSubL"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Input Box
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    placeholder = { Text(text = LanguageText.getText(curLang, "authEmailPh")) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "EmailIcon", tint = Color.Black.copy(alpha = 0.5f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color(0xFF007AFF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input Box
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localError = null
                    },
                    placeholder = { Text(text = LanguageText.getText(curLang, "authPassPh")) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PasswordIcon", tint = Color.Black.copy(alpha = 0.5f)) },
                    trailingIcon = {
                        IconButton(onClick = { pwordVisible = !pwordVisible }) {
                            Icon(
                                imageVector = if (pwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    },
                    visualTransformation = if (pwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.7f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color(0xFF007AFF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Notification Box
                AnimatedVisibility(
                    visible = localError != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error notification",
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localError ?: "",
                            color = Color(0xFFFF3B30),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Button Action
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val emailClean = email.trim()
                        val pClean = password.trim()
                        if (emailClean.isEmpty() || pClean.isEmpty()) {
                            localError = "Please fill in all fields"
                            return@Button
                        }
                        if (isSignUp) {
                            viewModel.signUp(emailClean, pClean, {}, { localError = it })
                        } else {
                            viewModel.login(emailClean, pClean, {}, { localError = it })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUp) Color(0xFF34C759) else Color(0xFF007AFF)
                    ),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text(
                            text = LanguageText.getText(
                                curLang,
                                if (isSignUp) "authSignUpBtn" else "authLoginBtn"
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Mode Alternator link text
                Text(
                    text = LanguageText.getText(
                        curLang,
                        if (isSignUp) "authToggleS" else "authToggleL"
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF007AFF),
                    modifier = Modifier
                        .clickable {
                            isSignUp = !isSignUp
                            localError = null
                        }
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
