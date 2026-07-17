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
import com.core2studio.mymanager.data.utils.CurrencyUtils
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
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headingPaint = Paint().apply {
        color = deepSlate
        textSize = 20f
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

    private fun drawBusinessHeader(canvas: Canvas, businessName: String, businessEmail: String, businessPhone: String, businessAddress: String, businessWebsite: String = ""): Float {
        var y = MARGIN + 10f

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 120f, bgPaint)

        canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 15f, titlePaint)
        y += 25f

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
        if (businessWebsite.isNotBlank()) {
            canvas.drawText(businessWebsite, MARGIN, y, smallPaint)
            y += 14f
        }

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 25f

        return y
    }

    private fun drawImageGrid(canvas: Canvas, imageUrls: List<String>, startY: Float): Float {
        val images = imageUrls.take(4)
        if (images.isEmpty()) return startY

        val gap = 10f
        val imageWidth = (PAGE_WIDTH - 2 * MARGIN - gap) / 2f
        val imageHeight = 240f

        var y = startY
        var col = 0

        for (url in images) {
            val imageX = if (col % 2 == 0) MARGIN else MARGIN + imageWidth + gap
            val bitmap = downloadImage(url)
            if (bitmap != null) {
                val scaled = scaleBitmap(bitmap, imageWidth, imageHeight)
                val destRect = RectF(imageX, y, imageX + scaled.width, y + scaled.height)
                canvas.drawRoundRect(destRect, 8f, 8f, imageBgPaint)
                canvas.drawBitmap(scaled, null, destRect, null)
            } else {
                canvas.drawRoundRect(RectF(imageX, y, imageX + imageWidth, y + imageHeight), 8f, 8f, imageBgPaint)
            }
            col++
            if (col % 2 == 0) {
                y += imageHeight + gap
            }
        }
        if (col % 2 != 0) {
            y += imageHeight + gap
        }

        return y + 10f
    }

    private fun drawProductDetails(canvas: Canvas, product: Product, startY: Float): Float {
        var y = startY

        canvas.drawText(product.name, MARGIN, y, headingPaint)
        y += 25f

        val priceText = CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(context))
        canvas.drawText(priceText, MARGIN, y, pricePaint)
        y += 30f

        if (product.description.isNotBlank()) {
            val lines = wrapText(product.description, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
            for (line in lines) {
                if (y > PAGE_HEIGHT - 80) break
                canvas.drawText(line, MARGIN, y, bodyPaint)
                y += LINE_HEIGHT
            }
        }

        return y
    }

    private fun drawFooter(canvas: Canvas, pageNum: Int, totalPages: Int = 0) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
        val footerText = if (totalPages > 0) {
            "Generated by MyManager • ${dateFormat.format(Date())} • Page $pageNum/$totalPages"
        } else {
            "Generated by MyManager • ${dateFormat.format(Date())}"
        }
        val footerWidth = smallPaint.measureText(footerText)
        canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)
    }

    suspend fun generateSingleProductPdf(
        product: Product,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = "",
        businessWebsite: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var y = drawBusinessHeader(canvas, businessName, businessEmail, businessPhone, businessAddress, businessWebsite)

            canvas.drawText(product.name, MARGIN, y, headingPaint)
            y += 30f

            val priceText = CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(context))
            canvas.drawText(priceText, MARGIN, y, pricePaint)
            y += 35f

            y = drawImageGrid(canvas, product.imageUrls, y)

            if (product.description.isNotBlank()) {
                val lines = wrapText(product.description, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
                for (line in lines) {
                    if (y > PAGE_HEIGHT - 80) break
                    canvas.drawText(line, MARGIN, y, bodyPaint)
                    y += LINE_HEIGHT
                }
            }

            drawFooter(canvas, 1)

            document.finishPage(page)

            val fileName = "MyManager_${product.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            document.close()
            e.printStackTrace()
            null
        }
    }

    suspend fun generateMultiProductPdf(
        products: List<Product>,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = "",
        businessWebsite: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        try {
            val totalPages = products.size
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            products.forEachIndexed { index, product ->
                val pageNum = index + 1
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var y = drawBusinessHeader(canvas, businessName, businessEmail, businessPhone, businessAddress, businessWebsite)

                canvas.drawText(product.name, MARGIN, y, headingPaint)
                y += 30f

                val priceText = CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(context))
                canvas.drawText(priceText, MARGIN, y, pricePaint)
                y += 35f

                y = drawImageGrid(canvas, product.imageUrls, y)

                if (product.description.isNotBlank()) {
                    val lines = wrapText(product.description, bodyPaint, PAGE_WIDTH - 2 * MARGIN)
                    for (line in lines) {
                        if (y > PAGE_HEIGHT - 80) break
                        canvas.drawText(line, MARGIN, y, bodyPaint)
                        y += LINE_HEIGHT
                    }
                }

                // Footer with page number
                canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
                val footerText = "Generated by MyManager • ${dateFormat.format(Date())} • Page $pageNum/$totalPages"
                val footerWidth = smallPaint.measureText(footerText)
                canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

                document.finishPage(page)
            }

            val fileName = "MyManager_Catalog_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            document.close()
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
        businessAddress: String = "",
        businessWebsite: String = ""
    ): Uri? = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        try {
            val itemPages = cartItems.size
            val totalPages = itemPages + 1
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            val tableWidth = PAGE_WIDTH - 2 * MARGIN
            val snColWidth = 50f
            val qtyColWidth = 75f
            val priceColWidth = 75f
            val itemColWidth = tableWidth - snColWidth - qtyColWidth - priceColWidth

            val snColX = MARGIN
            val itemColX = MARGIN + snColWidth
            val qtyColX = MARGIN + snColWidth + itemColWidth
            val priceColX = MARGIN + snColWidth + itemColWidth + qtyColWidth

            val tableBottom = PAGE_HEIGHT - 60f

            val headerPaint = Paint().apply {
                color = white
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val valuePaint = Paint().apply {
                color = deepSlate
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val tableLinePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#CCCCCC")
                strokeWidth = 0.5f
                isAntiAlias = true
            }

            val headerBgPaint = Paint().apply {
                color = forestGreen
                style = Paint.Style.FILL
            }

            fun drawTableHeader(canvas: Canvas, y: Float) {
                val rowHeight = 30f
                canvas.drawRect(snColX, y, snColX + tableWidth, y + rowHeight, headerBgPaint)
                canvas.drawText("S/no.", snColX + snColWidth / 2f, y + 20f, headerPaint)
                canvas.drawText("Item", itemColX + itemColWidth / 2f, y + 20f, headerPaint)
                canvas.drawText("Quantity", qtyColX + qtyColWidth / 2f, y + 20f, headerPaint)
                canvas.drawText("Price(${CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))})", priceColX + priceColWidth / 2f, y + 20f, headerPaint)
                canvas.drawLine(snColX, y + rowHeight, snColX + tableWidth, y + rowHeight, tableLinePaint)
            }

            fun drawVerticalLines(canvas: Canvas, y: Float, bottom: Float) {
                canvas.drawLine(snColX, y, snColX, bottom, tableLinePaint)
                canvas.drawLine(itemColX, y, itemColX, bottom, tableLinePaint)
                canvas.drawLine(qtyColX, y, qtyColX, bottom, tableLinePaint)
                canvas.drawLine(priceColX, y, priceColX, bottom, tableLinePaint)
                canvas.drawLine(priceColX + priceColWidth, y, priceColX + priceColWidth, bottom, tableLinePaint)
            }

            cartItems.forEachIndexed { index, item ->
                val pageNum = index + 1
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var y = drawBusinessHeader(canvas, businessName, businessEmail, businessPhone, businessAddress, businessWebsite)

                if (pageNum == 1) {
                    drawTableHeader(canvas, y)
                    y += 30f
                }

                val itemRowTop = y
                val snTextY = y + 30f

                val nameLines = wrapText(item.productName, headingPaint, itemColWidth - 20f)
                var nameY = y + 25f
                for (line in nameLines.take(2)) {
                    canvas.drawText(line, itemColX + 10f, nameY, headingPaint)
                    nameY += 20f
                }

                val imageTopY = nameY + 10f
                val imageMaxWidth = itemColWidth - 20f
                val imageMaxHeight = tableBottom - imageTopY - 10f

                if (item.productImage.isNotBlank()) {
                    val bitmap = downloadImage(item.productImage)
                    if (bitmap != null) {
                        val scaled = scaleBitmap(bitmap, imageMaxWidth, imageMaxHeight)
                        val imageX = itemColX + (itemColWidth - scaled.width) / 2f
                        val imageY = imageTopY + (imageMaxHeight - scaled.height) / 2f
                        val destRect = RectF(imageX, imageY, imageX + scaled.width, imageY + scaled.height)
                        canvas.drawRoundRect(destRect, 8f, 8f, imageBgPaint)
                        canvas.drawBitmap(scaled, null, destRect, null)
                    } else {
                        canvas.drawRoundRect(
                            RectF(itemColX + 10f, imageTopY, itemColX + itemColWidth - 10f, tableBottom - 10f),
                            8f, 8f, imageBgPaint
                        )
                    }
                } else {
                    canvas.drawRoundRect(
                        RectF(itemColX + 10f, imageTopY, itemColX + itemColWidth - 10f, tableBottom - 10f),
                        8f, 8f, imageBgPaint
                    )
                }

                canvas.drawText("$pageNum", snColX + snColWidth / 2f, snTextY, valuePaint)

                val qtyText = "${item.quantity}"
                canvas.drawText(qtyText, qtyColX + qtyColWidth / 2f, snTextY, valuePaint)

                val lineTotal = item.price * item.quantity
                val priceText = CurrencyUtils.formatCurrency(lineTotal, CurrencyUtils.loadCurrencyCode(context))
                canvas.drawText(priceText, priceColX + priceColWidth / 2f, snTextY, valuePaint)

                drawVerticalLines(canvas, itemRowTop, tableBottom)

                canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
                val footerText = "Generated by MyManager \u2022 ${dateFormat.format(Date())} \u2022 Page $pageNum/$totalPages"
                val footerWidth = smallPaint.measureText(footerText)
                canvas.drawText(footerText, (PAGE_WIDTH - footerWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

                document.finishPage(page)
            }

            val summaryPageNum = totalPages
            val summaryPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, summaryPageNum).create()
            val summaryPage = document.startPage(summaryPageInfo)
            val summaryCanvas = summaryPage.canvas

            var sy = drawBusinessHeader(summaryCanvas, businessName, businessEmail, businessPhone, businessAddress, businessWebsite)

            summaryCanvas.drawLine(snColX, sy, snColX + tableWidth, sy, tableLinePaint)
            sy += 40f

            summaryCanvas.drawText("Grand Total", itemColX + itemColWidth / 2f, sy, headingPaint)
            val grandTotalText = CurrencyUtils.formatCurrency(cartTotal, CurrencyUtils.loadCurrencyCode(context))
            summaryCanvas.drawText(grandTotalText, priceColX + priceColWidth / 2f, sy, valuePaint)
            sy += 20f
            summaryCanvas.drawLine(snColX, sy, snColX + tableWidth, sy, tableLinePaint)

            drawVerticalLines(summaryCanvas, sy - 40f, sy)

            sy += 50f
            val notePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#888888")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                isAntiAlias = true
            }
            summaryCanvas.drawText("Note*: Additional Charges not included", MARGIN, sy, notePaint)

            summaryCanvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
            val summaryFooterText = "Generated by MyManager \u2022 ${dateFormat.format(Date())} \u2022 Page $summaryPageNum/$totalPages"
            val summaryFooterWidth = smallPaint.measureText(summaryFooterText)
            summaryCanvas.drawText(summaryFooterText, (PAGE_WIDTH - summaryFooterWidth) / 2f, PAGE_HEIGHT - 40f, smallPaint)

            document.finishPage(summaryPage)

            val fileName = "MyManager_Cart_${System.currentTimeMillis()}.pdf"
            savePdf(document, fileName)
        } catch (e: Exception) {
            document.close()
            e.printStackTrace()
            null
        }
    }

    private fun downloadImage(url: String): Bitmap? {
        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes() ?: return@use null
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else {
                    null
                }
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
