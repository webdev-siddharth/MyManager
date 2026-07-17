package com.core2studio.mymanager.ui.screens.auth

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BusinessInfoSetupScreen(
    viewModel: AuthViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Icon(
            imageVector = Icons.Filled.Business,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Set Up Your Business",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tell us about your business to get started",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.pendingBusinessName,
            onValueChange = { viewModel.updatePendingBusinessName(it) },
            label = { Text("Business Name *") },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your business name") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.pendingBusinessEmail,
            onValueChange = { viewModel.updatePendingBusinessEmail(it) },
            label = { Text("Business Email *") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your business email") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.pendingBusinessPhone,
            onValueChange = { viewModel.updatePendingBusinessPhone(it) },
            label = { Text("Business Phone *") },
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your business phone") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.pendingBusinessAddress,
            onValueChange = { viewModel.updatePendingBusinessAddress(it) },
            label = { Text("Business Address *") },
            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your business address") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.pendingGstin,
            onValueChange = { viewModel.updatePendingGstin(it) },
            label = { Text("GSTIN (optional)") },
            leadingIcon = { Icon(Icons.Filled.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter GSTIN") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.pendingWebsite,
            onValueChange = { viewModel.updatePendingWebsite(it) },
            label = { Text("Website (optional)") },
            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.outline) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Enter your website URL") }
        )

        state.error?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveBusinessInfoAndContinue() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continue", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
