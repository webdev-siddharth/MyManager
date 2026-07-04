package com.core2studio.mymanager.ui.screens.settings

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ErrorRed
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@Composable
fun BusinessInfoScreen(
    businessName: String = "",
    businessEmail: String = "",
    businessPhone: String = "",
    businessAddress: String = "",
    businessLogoUrl: String = "",
    gstin: String = "",
    isUploadingLogo: Boolean = false,
    onSaveBusinessInfo: (name: String, email: String, phone: String, address: String, gstin: String) -> Unit = { _, _, _, _, _ -> },
    onUploadLogo: (Uri) -> Unit = {},
    onClearLogo: () -> Unit = {},
    onViewBusinessCard: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteLogoDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }

    var localBusinessName by remember { mutableStateOf(businessName) }
    var localBusinessEmail by remember { mutableStateOf(businessEmail) }
    var localBusinessPhone by remember { mutableStateOf(businessPhone) }
    var localBusinessAddress by remember { mutableStateOf(businessAddress) }
    var localGstin by remember { mutableStateOf(gstin) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onUploadLogo(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            val path = android.provider.MediaStore.Images.Media.insertImage(
                context.contentResolver, it, "business_logo_${System.currentTimeMillis()}", null
            )
            path?.let { uri -> onUploadLogo(Uri.parse(uri)) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    LaunchedEffect(businessName, businessEmail, businessPhone, businessAddress, gstin) {
        localBusinessName = businessName
        localBusinessEmail = businessEmail
        localBusinessPhone = businessPhone
        localBusinessAddress = businessAddress
        localGstin = gstin
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = if (isEditMode) "Edit Business Info" else "Business Info",
                onBackClick = {
                    if (isEditMode) {
                        isEditMode = false
                        localBusinessName = businessName
                        localBusinessEmail = businessEmail
                        localBusinessPhone = businessPhone
                        localBusinessAddress = businessAddress
                        localGstin = gstin
                    } else {
                        onBack()
                    }
                }
            )
        },
        containerColor = MintCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Logo Section
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PaleMint)
                            .border(2.dp, SageGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (businessLogoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = businessLogoUrl,
                                contentDescription = "Business Logo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "Business Logo",
                                modifier = Modifier.size(48.dp),
                                tint = SageGreen
                            )
                        }

                        if (isUploadingLogo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = ForestGreen
                            )
                        }
                    }

                    // Upload button - bottom right
                    if (isEditMode) {
                        IconButton(
                            onClick = { showImageSourceDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Change logo",
                                tint = White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isEditMode) {
                // EDIT MODE - Single card, all fields
                MyManagerCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                        Text(
                            text = "Edit Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = localBusinessName,
                        onValueChange = { localBusinessName = it },
                        label = { Text("Business Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors,
                        placeholder = { Text("Enter your business name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = localBusinessEmail,
                        onValueChange = { localBusinessEmail = it },
                        label = { Text("Business Email*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors,
                        placeholder = { Text("Enter your business email") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = localBusinessPhone,
                        onValueChange = { localBusinessPhone = it },
                        label = { Text("Business Phone*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors,
                        placeholder = { Text("Enter your business phone") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = localBusinessAddress,
                        onValueChange = { localBusinessAddress = it },
                        label = { Text("Business Address*") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = textFieldColors,
                        placeholder = { Text("Enter your business address") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = localGstin,
                        onValueChange = { localGstin = it },
                        label = { Text("GSTIN (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors,
                        placeholder = { Text("Enter GSTIN") }
                    )
                }

                Button(
                    onClick = {
                        onSaveBusinessInfo(
                            localBusinessName,
                            localBusinessEmail,
                            localBusinessPhone,
                            localBusinessAddress,
                            localGstin
                        )
                        isEditMode = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Business Info")
                }
            } else {
                // VIEW MODE
                MyManagerCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                        Text(
                            text = "Business Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (businessName.isNotEmpty()) {
                        InfoRow(icon = Icons.Filled.Store, label = "Name", value = businessName)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (businessEmail.isNotEmpty()) {
                        InfoRow(icon = Icons.Filled.Email, label = "Email", value = businessEmail)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (businessPhone.isNotEmpty()) {
                        InfoRow(icon = Icons.Filled.Phone, label = "Phone", value = businessPhone)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (businessAddress.isNotEmpty()) {
                        InfoRow(icon = Icons.Filled.LocationOn, label = "Address", value = businessAddress)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (gstin.isNotEmpty()) {
                        InfoRow(icon = Icons.Filled.CreditCard, label = "GSTIN", value = gstin)
                    }

                    if (businessName.isEmpty() && businessEmail.isEmpty() &&
                        businessPhone.isEmpty() && businessAddress.isEmpty() && gstin.isEmpty()
                    ) {
                        Text(
                            text = "No business info added yet. Tap Edit to add your details.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepSlate.copy(alpha = 0.6f)
                        )
                    }
                }

                Button(
                    onClick = onViewBusinessCard,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Business Card")
                }

                OutlinedButton(
                    onClick = { isEditMode = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Business Info")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Image source chooser dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Change Logo") },
            text = { Text("Select how you want to update your business logo") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }) {
                        Text("Gallery", color = ForestGreen)
                    }
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }) {
                        Text("Camera", color = ForestGreen)
                    }
                    if (businessLogoUrl.isNotEmpty()) {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            showDeleteLogoDialog = true
                        }) {
                            Text("Remove", color = ErrorRed)
                        }
                    }
                }
            },
            dismissButton = {}
        )
    }

    // Delete logo confirmation dialog
    if (showDeleteLogoDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteLogoDialog = false },
            title = { Text("Remove Logo") },
            text = { Text("Are you sure you want to remove your business logo?") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLogo()
                        showDeleteLogoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Remove", color = White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteLogoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SageGreen,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SageGreen
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DeepSlate
            )
        }
    }
}

private val PaleMint = com.core2studio.mymanager.theme.PaleMint
