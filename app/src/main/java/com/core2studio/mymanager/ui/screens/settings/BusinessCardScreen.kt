package com.core2studio.mymanager.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@Composable
fun BusinessCardScreen(
    businessName: String = "",
    businessEmail: String = "",
    businessPhone: String = "",
    businessAddress: String = "",
    businessLogoUrl: String = "",
    gstin: String = "",
    website: String = "",
    displayName: String = "",
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
        containerColor = MaterialTheme.colorScheme.background,
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
                            gstin = gstin,
                            website = website,
                            displayName = displayName
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // GSTIN top-right
                    if (gstin.isNotEmpty()) {
                        Text(
                            text = "GSTIN: $gstin",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Left column - Text info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Business Name
                            Text(
                                text = businessName.ifEmpty { "Your Business" },
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // User Name
                            if (displayName.isNotEmpty()) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Contact rows with Material Icons
                            if (businessPhone.isNotEmpty()) {
                                CardInfoRow(icon = Icons.Filled.Phone, value = businessPhone)
                            }
                            if (businessEmail.isNotEmpty()) {
                                CardInfoRow(icon = Icons.Filled.Email, value = businessEmail)
                            }
                            if (businessAddress.isNotEmpty()) {
                                CardInfoRow(icon = Icons.Filled.LocationOn, value = businessAddress)
                            }
                            if (website.isNotEmpty()) {
                                CardInfoRow(icon = Icons.Filled.Public, value = website)
                            }
                        }

                        // Right column - Logo
                        if (businessLogoUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = businessLogoUrl,
                                    contentDescription = "Business Logo",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Footer
                    Text(
                        text = "Created in MyManager",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap the share button to send your business card",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardInfoRow(
    icon: ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

private suspend fun shareBusinessCard(
    context: Context,
    businessName: String,
    businessEmail: String,
    businessPhone: String,
    businessAddress: String,
    businessLogoUrl: String,
    gstin: String,
    website: String,
    displayName: String
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
                gstin = gstin,
                website = website,
                displayName = displayName
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

            val shareText = buildString {
                append("Here's my business card:\n\n")
                append(businessName)
                if (displayName.isNotEmpty()) append("\n$displayName")
                if (businessPhone.isNotEmpty()) append("\nPhone: $businessPhone")
                if (businessEmail.isNotEmpty()) append("\nEmail: $businessEmail")
                if (businessAddress.isNotEmpty()) append("\nAddress: $businessAddress")
                if (gstin.isNotEmpty()) append("\nGSTIN: $gstin")
                if (website.isNotEmpty()) append("\nWebsite: $website")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Business Card - $businessName")
                putExtra(Intent.EXTRA_TEXT, shareText)
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
    gstin: String,
    website: String,
    displayName: String
): Bitmap {
    val width = 800
    val height = 480
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Pure white background
    val bgPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val deepSlate = android.graphics.Color.parseColor("#2C3E50")
    val forestGreen = android.graphics.Color.parseColor("#2D6A4F")
    val sageGreen = android.graphics.Color.parseColor("#40916C")

    // --- Left Column ---

    // Business Name
    val namePaint = Paint().apply {
        color = forestGreen
        textSize = 40f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(
        businessName.ifEmpty { "Your Business" },
        50f,
        90f,
        namePaint
    )

    // User Name (displayName)
    if (displayName.isNotEmpty()) {
        val userNamePaint = Paint().apply {
            color = sageGreen
            textSize = 24f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(displayName, 50f, 125f, userNamePaint)
    }

    // Contact info - text only, no icons
    val textPaint = Paint().apply {
        color = deepSlate
        textSize = 18f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    var y = 190f
    val leftMargin = 50f
    val maxWidth = 420f

    if (businessPhone.isNotEmpty()) {
        canvas.drawText(businessPhone, leftMargin, y, textPaint)
        y += 35f
    }
    if (businessEmail.isNotEmpty()) {
        canvas.drawText(businessEmail, leftMargin, y, textPaint)
        y += 35f
    }
    if (businessAddress.isNotEmpty()) {
        val words = businessAddress.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (textPaint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, leftMargin, y, textPaint)
                y += 26f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, leftMargin, y, textPaint)
            y += 35f
        }
    }
    if (website.isNotEmpty()) {
        canvas.drawText(website, leftMargin, y, textPaint)
    }

    // --- Right Column - Logo ---
    if (businessLogoUrl.isNotEmpty()) {
        try {
            val request = Request.Builder().url(businessLogoUrl).build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                    if (bytes != null) {
                        val logoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (logoBitmap != null) {
                            val logoSize = 180f
                            val cx = width - 70f - logoSize / 2f
                            val cy = height / 2f

                            // Clip to circle
                            val saveCount = canvas.saveLayer(
                                cx - logoSize / 2f, cy - logoSize / 2f,
                                cx + logoSize / 2f, cy + logoSize / 2f,
                                null
                            )
                            val circlePaint = Paint().apply { isAntiAlias = true }
                            canvas.drawCircle(cx, cy, logoSize / 2f, circlePaint)
                            circlePaint.xfermode = android.graphics.PorterDuffXfermode(
                                android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            val scaled = Bitmap.createScaledBitmap(
                                logoBitmap,
                                logoSize.toInt(),
                                logoSize.toInt(),
                                true
                            )
                            canvas.drawBitmap(
                                scaled,
                                cx - logoSize / 2f,
                                cy - logoSize / 2f,
                                circlePaint
                            )
                            canvas.restoreToCount(saveCount)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Logo download failed, leave blank space
        }
    }

    // --- Top Right - GSTIN ---
    if (gstin.isNotEmpty()) {
        val gstinPaint = Paint().apply {
            color = sageGreen
            textSize = 16f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val gstinText = "GSTIN: $gstin"
        val gstinWidth = gstinPaint.measureText(gstinText)
        canvas.drawText(gstinText, width - 50f - gstinWidth, 35f, gstinPaint)
    }

    // --- Bottom Center - Footer ---
    val footerPaint = Paint().apply {
        color = sageGreen
        textSize = 14f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }
    val footerText = "Created in MyManager"
    val footerWidth = footerPaint.measureText(footerText)
    canvas.drawText(footerText, (width - footerWidth) / 2f, height - 20f, footerPaint)

    return bitmap
}
