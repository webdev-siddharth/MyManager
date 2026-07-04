package com.core2studio.mymanager.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Order
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates flat-design PDF invoices using Android's native PdfDocument API.
 * Saves invoices to the device's Downloads folder.
 */
class InvoiceGenerator(
    private val context: Context
) {

    companion object {
        private const val PAGE_WIDTH = 595  // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f
    }

    // Colors matching Emerald Insights palette
    private val deepSlate = android.graphics.Color.parseColor("#2C3E50")
    private val forestGreen = android.graphics.Color.parseColor("#2D6A4F")
    private val sageGreen = android.graphics.Color.parseColor("#40916C")
    private val limeAccent = android.graphics.Color.parseColor("#4CAF50")
    private val paleMint = android.graphics.Color.parseColor("#E8F5E9")
    private val mintCream = android.graphics.Color.parseColor("#F0F4F8")
    private val white = android.graphics.Color.WHITE

    // Paint objects
    private val titlePaint = Paint().apply {
        color = forestGreen
        textSize = 24f
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

    private val smallPaint = Paint().apply {
        color = sageGreen
        textSize = 10f
        typeface = Typeface.DEFAULT
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

    private val linePaint = Paint().apply {
        color = sageGreen
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val bgPaint = Paint().apply {
        color = paleMint
        style = Paint.Style.FILL
    }

    /**
     * Generates a PDF invoice for a transaction and saves to Downloads.
     *
     * @param transaction The transaction to invoice
     * @param client The client for this transaction
     * @param productName The name of the associated product (optional)
     * @param businessName The business name from settings
     * @param businessEmail The business email from settings
     * @param businessPhone The business phone from settings
     * @param businessAddress The business address from settings
     * @return The file name of the generated PDF, or null on failure
     */
    fun generateInvoice(
        transaction: Order,
        client: Client,
        productName: String?,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = ""
    ): String? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 10f

        // --- Header Background ---
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 160f, bgPaint)

        // --- Business Name / Title ---
        canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 20f, titlePaint)
        y += 30f

        // --- Business Contact Info ---
        if (businessPhone.isNotBlank()) {
            canvas.drawText("Phone: $businessPhone", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (businessEmail.isNotBlank()) {
            canvas.drawText("Email: $businessEmail", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (businessAddress.isNotBlank()) {
            canvas.drawText("Address: $businessAddress", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (businessPhone.isNotBlank() || businessEmail.isNotBlank() || businessAddress.isNotBlank()) {
            y += 10f
        }

        // --- Invoice Label ---
        val invoiceLabel = "INVOICE"
        val labelWidth = headingPaint.measureText(invoiceLabel)
        canvas.drawText(invoiceLabel, PAGE_WIDTH - MARGIN - labelWidth, y, headingPaint)

        // Invoice number and date
        y += 20f
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val invoiceDate = dateFormat.format(Date(transaction.date))
        val invoiceNumber = "INV-${transaction.id.toString().padStart(5, '0')}"

        canvas.drawText("Invoice #: $invoiceNumber", MARGIN, y, bodyPaint)
        val dateText = "Date: $invoiceDate"
        val dateWidth = bodyPaint.measureText(dateText)
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN - dateWidth, y, bodyPaint)

        y += 30f

        // --- Divider ---
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // --- Client Details ---
        canvas.drawText("BILL TO", MARGIN, y, smallPaint)
        y += LINE_HEIGHT
        canvas.drawText(client.name, MARGIN, y, headingPaint)
        y += LINE_HEIGHT

        if (client.phone.isNotBlank()) {
            canvas.drawText("Phone: ${client.phone}", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (client.email.isNotBlank()) {
            canvas.drawText("Email: ${client.email}", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (client.address.isNotBlank()) {
            canvas.drawText("Address: ${client.address}", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }

        y += 20f

        // --- Divider ---
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // --- Line Items Table Header ---
        val col1 = MARGIN
        val col2 = MARGIN + 250f
        val col3 = MARGIN + 370f
        val col4 = PAGE_WIDTH - MARGIN

        // Table header background
        canvas.drawRect(MARGIN - 5f, y - 14f, PAGE_WIDTH - MARGIN + 5f, y + 6f, bgPaint)

        canvas.drawText("ITEM", col1, y, boldBodyPaint)
        canvas.drawText("DETAILS", col2, y, boldBodyPaint)
        val amtHeader = "AMOUNT"
        val amtHeaderWidth = boldBodyPaint.measureText(amtHeader)
        canvas.drawText(amtHeader, col4 - amtHeaderWidth, y, boldBodyPaint)

        y += LINE_HEIGHT + 5f

        // --- Product Line ---
        if (productName != null) {
            canvas.drawText(productName, col1, y, bodyPaint)
            val amtText = "₹${String.format("%.2f", transaction.amount)}"
            val amtWidth = bodyPaint.measureText(amtText)
            canvas.drawText(amtText, col4 - amtWidth, y, bodyPaint)
            y += LINE_HEIGHT
        } else {
            canvas.drawText("Service/Custom", col1, y, bodyPaint)
            val amtText = "₹${String.format("%.2f", transaction.amount)}"
            val amtWidth = bodyPaint.measureText(amtText)
            canvas.drawText(amtText, col4 - amtWidth, y, bodyPaint)
            y += LINE_HEIGHT
        }

        // --- Custom Fields as line items ---
        val customFields = parseCustomFields(transaction.customFields)
        customFields.forEach { (key, value) ->
            canvas.drawText(key, col1 + 10f, y, smallPaint)
            canvas.drawText(value, col2, y, bodyPaint)
            y += LINE_HEIGHT
        }

        // --- Notes ---
        if (transaction.notes.isNotBlank()) {
            y += 5f
            canvas.drawText("Notes: ${transaction.notes}", col1 + 10f, y, smallPaint)
            y += LINE_HEIGHT
        }

        y += 10f

        // --- Divider ---
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // --- Totals Section ---
        val totalsX = PAGE_WIDTH - MARGIN - 200f

        canvas.drawText("Total Amount:", totalsX, y, boldBodyPaint)
        val totalText = "₹${String.format("%.2f", transaction.amount)}"
        val totalWidth = amountPaint.measureText(totalText)
        canvas.drawText(totalText, col4 - totalWidth, y, amountPaint)
        y += LINE_HEIGHT + 5f

        canvas.drawText("Amount Paid:", totalsX, y, bodyPaint)
        val paidText = "₹${String.format("%.2f", transaction.paidAmount)}"
        val paidWidth = bodyPaint.measureText(paidText)
        canvas.drawText(paidText, col4 - paidWidth, y, bodyPaint)
        y += LINE_HEIGHT + 5f

        val balance = transaction.amount - transaction.paidAmount
        val balancePaint = if (balance > 0) Paint(amountPaint).apply {
            color = android.graphics.Color.parseColor("#E74C3C")
        } else amountPaint

        canvas.drawText("Balance Due:", totalsX, y, boldBodyPaint)
        val balanceText = "₹${String.format("%.2f", balance)}"
        val balanceWidth = balancePaint.measureText(balanceText)
        canvas.drawText(balanceText, col4 - balanceWidth, y, balancePaint)
        y += LINE_HEIGHT + 15f

        // --- Status Badge ---
        val statusText = when (transaction.status) {
            "COMPLETED" -> "PAID"
            "PARTIAL" -> "PARTIALLY PAID"
            else -> "PAYMENT PENDING"
        }
        val statusPaint = Paint(boldBodyPaint).apply {
            color = when (transaction.status) {
                "COMPLETED" -> limeAccent
                "PARTIAL" -> sageGreen
                else -> android.graphics.Color.parseColor("#E74C3C")
            }
            textSize = 14f
        }
        val statusBgPaint = Paint().apply {
            color = when (transaction.status) {
                "COMPLETED" -> android.graphics.Color.parseColor("#E8F5E9")
                "PARTIAL" -> android.graphics.Color.parseColor("#FFF3E0")
                else -> android.graphics.Color.parseColor("#FFEBEE")
            }
            style = Paint.Style.FILL
        }

        val statusWidth = statusPaint.measureText(statusText)
        val statusX = PAGE_WIDTH - MARGIN - statusWidth - 20f
        canvas.drawRect(statusX - 10f, y - 14f, PAGE_WIDTH - MARGIN + 5f, y + 8f, statusBgPaint)
        canvas.drawText(statusText, statusX, y, statusPaint)

        y += 40f

        // --- Footer ---
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
        val footerText = "Generated by MyManager • ${dateFormat.format(Date())}"
        val footerWidth = smallPaint.measureText(footerText)
        canvas.drawText(
            footerText,
            (PAGE_WIDTH - footerWidth) / 2f,
            PAGE_HEIGHT - 40f,
            smallPaint
        )

        document.finishPage(page)

        // Save to Downloads
        val fileName = "MyManager_Invoice_${invoiceNumber}_${System.currentTimeMillis()}.pdf"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for API 29+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { outputStream ->
                    document.writeTo(outputStream)
                }
            }

            document.close()
            fileName
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    private fun parseCustomFields(json: String): Map<String, String> {
        return try {
            Json.decodeFromString<Map<String, String>>(json)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
