package com.core2studio.mymanager.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.core2studio.mymanager.data.local.entity.CartItem
import com.core2studio.mymanager.data.local.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProductShareGenerator(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f
        private const val IMAGE_MAX_WIDTH = 250f
        private const val IMAGE_MAX_HEIGHT = 200f
    }

    private val deepSlate = android.graphics.Color.parseColor("#2C3E50")
    private val forestGreen = android.graphics.Color.parseColor("#2D6A4F")
    private val sageGreen = android.graphics.Color.parseColor("#40916C")
    private val paleMint = android.graphics.Color.parseColor("#E8F5E9")
    private val white = android.graphics.Color.WHITE

    private val titlePaint = Paint().apply {
        color = forestGreen
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headingPaint = Paint().apply {
        color = deepSlate
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val bodyPaint = Paint().apply {
        color = deepSlate
        textSize = 12f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val pricePaint = Paint().apply {
        color = forestGreen
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val boldBodyPaint = Paint().apply {
        color = deepSlate
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val amountPaint = Paint().apply {
        color = forestGreen
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val smallPaint = Paint().apply {
        color = deepSlate
        textSize = 11f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = sageGreen
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = paleMint
        style = Paint.Style.FILL
    }

    private val imageBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
    }

    private val qtyValuePaint = Paint().apply {
        color = deepSlate
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val qtyLabelPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#888888")
        textSize = 9f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun generateSingleProductPdf(
        product: Product,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = MARGIN + 10f

            // Header background
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, bgPaint)

            // Business name
            canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 15f, titlePaint)
            y += 25f

            // Business info - each on its own line
            if (businessPhone.isNotBlank()) {
                canvas.drawText(businessPhone, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessEmail.isNotBlank()) {
                canvas.drawText(businessEmail, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessAddress.isNotBlank()) {
                canvas.drawText(businessAddress, MARGIN, y, smallPaint)
                y += 14f
            }

            y += 20f

            // Divider
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 25f

            // Product images (all of them, 2 columns)
            if (product.imageUrls.isNotEmpty()) {
                val maxImageWidth = (PAGE_WIDTH - 2 * MARGIN - 10f) / 2f
                val maxImageHeight = 150f
                var imageX = MARGIN
                var imageRowY = y
                var col = 0
                for (url in product.imageUrls) {
                    if (y > PAGE_HEIGHT - 150) break
                    val bitmap = downloadImage(url) ?: continue
                    val scaled = scaleBitmap(bitmap, maxImageWidth, maxImageHeight)
                    val destRect = RectF(imageX, imageRowY, imageX + scaled.width, imageRowY + scaled.height)
                    canvas.drawRoundRect(destRect, 8f, 8f, imageBgPaint)
                    canvas.drawBitmap(scaled, null, destRect, null)
                    col++
                    if (col % 2 == 0) {
                        imageRowY += maxImageHeight + 10f
                        y = imageRowY
                        imageX = MARGIN
                    } else {
                        imageX += maxImageWidth + 10f
                    }
                }
                if (col % 2 != 0) {
                    imageRowY += maxImageHeight + 10f
                    y = imageRowY
                }
                y += 10f
            }

            // Product name
            canvas.drawText(product.name, MARGIN, y, headingPaint)
            y += 25f

            // Price
            val priceText = "₹${String.format(Locale.getDefault(), "%.2f", product.price)}"
            canvas.drawText(priceText, MARGIN, y, pricePaint)
            y += 30f

            // Description
            if (product.description.isNotBlank()) {
                val lines = wrapText(product.description, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
                for (line in lines) {
                    if (y > PAGE_HEIGHT - 80) break
                    canvas.drawText(line, MARGIN, y, bodyPaint)
                    y += LINE_HEIGHT
                }
            }

            // Footer
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
            val footerText = "Generated by MyManager • ${dateFormat.format(Date())}"
            val footerWidth = smallPaint.measureText(footerText)
            canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

            document.finishPage(page)

            val fileName = "MyManager_${product.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateMultiProductPdf(
        products: List<Product>,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            var pageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var y = MARGIN + 10f

            // Header background
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, bgPaint)

            // Business name
            canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 15f, titlePaint)
            y += 25f

            // Business info - each on its own line
            if (businessPhone.isNotBlank()) {
                canvas.drawText(businessPhone, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessEmail.isNotBlank()) {
                canvas.drawText(businessEmail, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessAddress.isNotBlank()) {
                canvas.drawText(businessAddress, MARGIN, y, smallPaint)
                y += 14f
            }

            y += 10f

            // Product count label
            canvas.drawText("Product Catalog — ${products.size} item(s)", MARGIN, y, smallPaint)
            y += 15f

            // Divider
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 25f

            products.forEachIndexed { index, product ->
                // Check if we need a new page (need at least 200pt for a product block)
                if (y > PAGE_HEIGHT - 220) {
                    // Footer on current page
                    canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val footerText = "Generated by MyManager • ${dateFormat.format(Date())} • Page $pageNum"
                    val footerWidth = smallPaint.measureText(footerText)
                    canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN + 20f
                }

                // Product number + name
                val numLabel = "${index + 1}."
                canvas.drawText(numLabel, MARGIN, y, smallPaint)
                canvas.drawText(product.name, MARGIN + 20f, y, headingPaint)
                y += 22f

                // Price
                val priceText = "₹${String.format(Locale.getDefault(), "%.2f", product.price)}"
                canvas.drawText(priceText, MARGIN + 20f, y, pricePaint)
                y += 22f

                // Description
                if (product.description.isNotBlank()) {
                    val lines = wrapText(product.description, bodyPaint, PAGE_WIDTH - 2 * MARGIN - 20f)
                    for (line in lines.take(3)) {
                        canvas.drawText(line, MARGIN + 20f, y, bodyPaint)
                        y += LINE_HEIGHT
                    }
                }

                // Image (smaller for multi-product)
                val imageBitmap = product.imageUrls.firstOrNull()?.let { url ->
                    downloadImage(url)
                }
                if (imageBitmap != null) {
                    val scaled = scaleBitmap(imageBitmap, 150f, 120f)
                    val imageX = MARGIN + 20f
                    val destRect = RectF(imageX, y, imageX + scaled.width, y + scaled.height)
                    canvas.drawRoundRect(destRect, 6f, 6f, imageBgPaint)
                    canvas.drawBitmap(scaled, null, destRect, null)
                    y += scaled.height + 10f
                }

                // Separator between products
                if (index < products.lastIndex) {
                    y += 5f
                    canvas.drawLine(MARGIN + 20f, y, PAGE_WIDTH - MARGIN, y, linePaint)
                    y += 15f
                }
            }

            // Footer on last page
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
            val footerText = "Generated by MyManager • ${dateFormat.format(Date())} • Page $pageNum"
            val footerWidth = smallPaint.measureText(footerText)
            canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

            document.finishPage(page)

            val fileName = "MyManager_Catalog_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun generateCartPdf(
        cartItems: List<CartItem>,
        cartTotal: Double,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val document = PdfDocument()
            var pageNum = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            var y = MARGIN + 10f

            // Header background
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, bgPaint)

            // Business name
            canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 15f, titlePaint)
            y += 25f

            // Business info
            if (businessPhone.isNotBlank()) {
                canvas.drawText(businessPhone, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessEmail.isNotBlank()) {
                canvas.drawText(businessEmail, MARGIN, y, smallPaint)
                y += 14f
            }
            if (businessAddress.isNotBlank()) {
                canvas.drawText(businessAddress, MARGIN, y, smallPaint)
                y += 14f
            }

            y += 10f

            // Cart label
            canvas.drawText("Cart Summary — ${cartItems.size} item(s)", MARGIN, y, smallPaint)
            y += 15f

            // Divider
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 25f

            // Cart items as cards with images
            val imageSide = 100f
            val nameStartX = MARGIN + imageSide + 12f
            val rightColWidth = 90f
            val rightColGap = 10f
            val rightSectionStart = PAGE_WIDTH - MARGIN - rightColWidth * 2 - rightColGap
            val nameMaxWidth = rightSectionStart - nameStartX - 8f

            for (item in cartItems) {
                if (y > PAGE_HEIGHT - 140) {
                    canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val footerText = "Generated by SnapBook • ${dateFormat.format(Date())} • Page $pageNum"
                    val footerWidth = smallPaint.measureText(footerText)
                    canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = MARGIN + 20f
                }

                // Draw card background
                val cardTop = y - 5f
                val cardBottom = y + imageSide + 5f
                canvas.drawRoundRect(
                    RectF(MARGIN - 5f, cardTop, PAGE_WIDTH - MARGIN + 5f, cardBottom),
                    6f, 6f, bgPaint
                )

                // Draw product image
                if (item.productImage.isNotBlank()) {
                    val bitmap = downloadImage(item.productImage)
                    if (bitmap != null) {
                        val scaled = scaleBitmap(bitmap, imageSide, imageSide)
                        val destRect = RectF(MARGIN, y, MARGIN + scaled.width, y + scaled.height)
                        canvas.save()
                        val path = android.graphics.Path().apply {
                            addRoundRect(destRect, 8f, 8f, android.graphics.Path.Direction.CW)
                        }
                        canvas.clipPath(path)
                        canvas.drawBitmap(scaled, null, destRect, null)
                        canvas.restore()
                    } else {
                        canvas.drawRoundRect(
                            RectF(MARGIN, y, MARGIN + imageSide, y + imageSide),
                            8f, 8f, imageBgPaint
                        )
                    }
                } else {
                    canvas.drawRoundRect(
                        RectF(MARGIN, y, MARGIN + imageSide, y + imageSide),
                        8f, 8f, imageBgPaint
                    )
                }

                // Product name (vertically centered with image)
                val nameLines = wrapText(item.productName, headingPaint, nameMaxWidth)
                val nameTotalHeight = nameLines.size * 18f
                var textY = y + (imageSide - nameTotalHeight) / 2f + 14f
                for (line in nameLines.take(2)) {
                    canvas.drawText(line, nameStartX, textY, headingPaint)
                    textY += 18f
                }

                // Right columns: QTY and PRICE side by side
                val colQtyX = rightSectionStart
                val colPriceX = rightSectionStart + rightColWidth + rightColGap

                // Qty column background
                canvas.drawRoundRect(
                    RectF(colQtyX, cardTop + 2f, colQtyX + rightColWidth, cardBottom - 2f),
                    4f, 4f, imageBgPaint
                )

                // Qty value
                val qtyText = "×${item.quantity}"
                val qtyWidth = qtyValuePaint.measureText(qtyText)
                canvas.drawText(qtyText, colQtyX + (rightColWidth - qtyWidth) / 2f, y + imageSide / 2f - 2f, qtyValuePaint)

                // Qty label
                canvas.drawText("QTY", colQtyX + rightColWidth / 2f, y + imageSide / 2f + 16f, qtyLabelPaint)

                // Price column background
                canvas.drawRoundRect(
                    RectF(colPriceX, cardTop + 2f, colPriceX + rightColWidth, cardBottom - 2f),
                    4f, 4f, imageBgPaint
                )

                // Price value
                val itemTotal = item.price * item.quantity
                val priceText = "₹${String.format(Locale.getDefault(), "%.0f", itemTotal)}"
                val priceWidth = qtyValuePaint.measureText(priceText)
                canvas.drawText(priceText, colPriceX + (rightColWidth - priceWidth) / 2f, y + imageSide / 2f - 2f, qtyValuePaint)

                // Price label
                canvas.drawText("TOTAL", colPriceX + rightColWidth / 2f, y + imageSide / 2f + 16f, qtyLabelPaint)

                y = cardBottom + 12f
            }

            y += 10f

            // Grand total
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 20f

            val grandTotalLabel = "Grand Total"
            canvas.drawText(grandTotalLabel, MARGIN, y, headingPaint)
            val grandTotalText = "₹${String.format(Locale.getDefault(), "%.2f", cartTotal)}"
            val grandTotalWidth = amountPaint.measureText(grandTotalText)
            canvas.drawText(grandTotalText, PAGE_WIDTH - MARGIN - grandTotalWidth, y, amountPaint)

            // Footer
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
            val footerText = "Generated by SnapBook • ${dateFormat.format(Date())} • Page $pageNum"
            val footerWidth = smallPaint.measureText(footerText)
            canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

            document.finishPage(page)

            val fileName = "SnapBook_Cart_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun downloadImage(url: String): Bitmap? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val bytes = response.body?.bytes() ?: return null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Float, maxHeight: Float): Bitmap {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val scale = minOf(maxWidth / width, maxHeight / height, 1f)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    private fun savePdf(document: PdfDocument, fileName: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "shared_pdfs")
            cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            document.close()
            e.printStackTrace()
            null
        }
    }
}
