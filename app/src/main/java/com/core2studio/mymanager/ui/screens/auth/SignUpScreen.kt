package com.core2studio.mymanager.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ErrorRed
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White

@Composable
fun SignUpScreen(
    viewModel: com.core2studio.mymanager.ui.screens.auth.AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = ForestGreen,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = DeepSlate.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.displayName,
            onValueChange = { viewModel.setDisplayName(it) },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = SageGreen) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SageGreen) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SageGreen) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = SageGreen
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { viewModel.updateConfirmPassword(it) },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SageGreen) },
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                        tint = SageGreen
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp)
        )

        state.error?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen, contentColor = White),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "or",
            style = MaterialTheme.typography.bodyMedium,
            color = DeepSlate.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.triggerGoogleSignIn(fromSignup = true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign up with Google", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Already have an account? Sign In",
            style = MaterialTheme.typography.bodyMedium,
            color = ForestGreen,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(enabled = !state.isLoading) { onNavigateToLogin() }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}
