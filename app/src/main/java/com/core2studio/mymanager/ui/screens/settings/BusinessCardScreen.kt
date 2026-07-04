package com.core2studio.mymanager.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun BusinessCardScreen(
    businessName: String = "",
    businessEmail: String = "",
    businessPhone: String = "",
    businessAddress: String = "",
    businessLogoUrl: String = "",
    gstin: String = "",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = "Business Card",
                onBackClick = onBack
            )
        },
        containerColor = MintCream,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        shareBusinessCard(
                            context = context,
                            businessName = businessName,
                            businessEmail = businessEmail,
                            businessPhone = businessPhone,
                            businessAddress = businessAddress,
                            businessLogoUrl = businessLogoUrl,
                            gstin = gstin
                        )
                    }
                },
                containerColor = ForestGreen,
                contentColor = White
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share Business Card"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Business Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, SageGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(PaleMint)
                            .border(2.dp, ForestGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (businessLogoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = businessLogoUrl,
                                contentDescription = "Business Logo",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "Business Logo",
                                modifier = Modifier.size(36.dp),
                                tint = ForestGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Business Name
                    Text(
                        text = businessName.ifEmpty { "Your Business" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepSlate
                    )

                    if (gstin.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "GSTIN: $gstin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SageGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SageGreen.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact Info
                    if (businessPhone.isNotEmpty()) {
                        CardRow(icon = Icons.Filled.Phone, value = businessPhone)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (businessEmail.isNotEmpty()) {
                        CardRow(icon = Icons.Filled.Email, value = businessEmail)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (businessAddress.isNotEmpty()) {
                        CardRow(icon = Icons.Filled.LocationOn, value = businessAddress)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SageGreen.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer
                    Text(
                        text = "Powered by MyManager",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap the share button to send your business card",
                style = MaterialTheme.typography.bodySmall,
                color = DeepSlate.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CardRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ForestGreen,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = DeepSlate
        )
    }
}

private suspend fun shareBusinessCard(
    context: Context,
    businessName: String,
    businessEmail: String,
    businessPhone: String,
    businessAddress: String,
    businessLogoUrl: String,
    gstin: String
) {
    withContext(Dispatchers.IO) {
        try {
            val bitmap = createBusinessCardBitmap(
                context = context,
                businessName = businessName,
                businessEmail = businessEmail,
                businessPhone = businessPhone,
                businessAddress = businessAddress,
                businessLogoUrl = businessLogoUrl,
                gstin = gstin
            )

            val cacheDir = File(context.cacheDir, "shared_images")
            cacheDir.mkdirs()
            val file = File(cacheDir, "business_card_${System.currentTimeMillis()}.png")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Business Card - $businessName")
                putExtra(Intent.EXTRA_TEXT, "Here's my business card:\n\n$businessName\n${if (businessPhone.isNotEmpty()) "Phone: $businessPhone\n" else ""}${if (businessEmail.isNotEmpty()) "Email: $businessEmail\n" else ""}${if (businessAddress.isNotEmpty()) "Address: $businessAddress\n" else ""}${if (gstin.isNotEmpty()) "GSTIN: $gstin\n" else ""}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(intent, "Share Business Card"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun createBusinessCardBitmap(
    context: Context,
    businessName: String,
    businessEmail: String,
    businessPhone: String,
    businessAddress: String,
    businessLogoUrl: String,
    gstin: String
): Bitmap {
    val width = 800
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background
    val bgPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 32f, 32f, bgPaint)

    // Border
    val borderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#40916C")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, 32f, 32f, borderPaint)

    // Green header background
    val headerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2D6A4F")
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, width.toFloat(), 160f, 32f, 32f, headerPaint)
    canvas.drawRect(0f, 120f, width.toFloat(), 160f, headerPaint)

    val deepSlate = android.graphics.Color.parseColor("#2C3E50")
    val white = android.graphics.Color.WHITE
    val sageGreen = android.graphics.Color.parseColor("#40916C")

    // Business Name
    val namePaint = Paint().apply {
        color = white
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(
        businessName.ifEmpty { "Your Business" },
        40f,
        80f,
        namePaint
    )

    // GSTIN
    if (gstin.isNotEmpty()) {
        val gstinPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E8F5E9")
            textSize = 18f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("GSTIN: $gstin", 40f, 120f, gstinPaint)
    }

    // Divider
    val dividerPaint = Paint().apply {
        color = sageGreen
        strokeWidth = 2f
    }
    canvas.drawLine(40f, 175f, width - 40f, 175f, dividerPaint)

    // Contact info
    var y = 220f
    val iconPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#2D6A4F")
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    val textPaint = Paint().apply {
        color = deepSlate
        textSize = 20f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    if (businessPhone.isNotEmpty()) {
        canvas.drawText("Phone", 40f, y, iconPaint)
        canvas.drawText(businessPhone, 140f, y, textPaint)
        y += 35f
    }
    if (businessEmail.isNotEmpty()) {
        canvas.drawText("Email", 40f, y, iconPaint)
        canvas.drawText(businessEmail, 140f, y, textPaint)
        y += 35f
    }
    if (businessAddress.isNotEmpty()) {
        canvas.drawText("Address", 40f, y, iconPaint)
        // Wrap address text
        val maxWidth = width - 180f
        val words = businessAddress.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (textPaint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, 140f, y, textPaint)
                y += 28f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, 140f, y, textPaint)
        }
    }

    // Footer
    val footerPaint = Paint().apply {
        color = sageGreen
        textSize = 14f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }
    val footerText = "Powered by MyManager"
    val footerWidth = footerPaint.measureText(footerText)
    canvas.drawText(footerText, (width - footerWidth) / 2f, height - 20f, footerPaint)

    return bitmap
}
