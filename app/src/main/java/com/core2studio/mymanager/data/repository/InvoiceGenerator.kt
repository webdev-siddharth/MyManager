package com.core2studio.mymanager.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.OrderItemDraft
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.utils.TaxCalculator
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

    data class InvoiceResult(
        val fileName: String,
        val uri: Uri
    )

    companion object {
        private const val PAGE_WIDTH = 595  // A4 width in points
        private const val PAGE_HEIGHT = 842 // A4 height in points
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 20f
    }

    // Colors matching Heartwood palette (light mode — PDFs print on white/light stock)
    private val deepSlate = android.graphics.Color.parseColor("#241A0F")
    private val forestGreen = android.graphics.Color.parseColor("#A85D18")
    private val sageGreen = android.graphics.Color.parseColor("#4F7A5C")
    private val paleMint = android.graphics.Color.parseColor("#FBF7EE")
    private val mintCream = android.graphics.Color.parseColor("#E9DCC0")
    private val white = android.graphics.Color.WHITE

    // Paint objects
    private val titlePaint = Paint().apply {
        color = forestGreen
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    private val headingPaint = Paint().apply {
        color = deepSlate
        textSize = 18f
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
     * @param businessWebsite The business website from settings
     * @param businessGstin The business GSTIN from settings
     * @param gstEnabled Whether the master GST toggle is enabled (live settings fallback)
     * @param gstPricingMode GST pricing mode fallback: "INCLUSIVE" or "EXCLUSIVE"
     * @param gstRate GST rate fallback (0, 3, 5, 18, 40); order customFields snapshot wins when present
     * @param gstType GST type fallback: "CGST_SGST" or "IGST"
     * @return InvoiceResult with file name and content URI, or null on failure
     */
    fun generateInvoice(
        transaction: Order,
        client: Client,
        productName: String?,
        businessName: String,
        businessEmail: String = "",
        businessPhone: String = "",
        businessAddress: String = "",
        businessWebsite: String = "",
        businessGstin: String = "",
        gstEnabled: Boolean = false,
        gstPricingMode: String = "INCLUSIVE",
        gstRate: Int = 18,
        gstType: String = "CGST_SGST"
    ): InvoiceResult? {
        // Parse custom fields and cart items ONCE at the start
        val customFields = parseCustomFields(transaction.customFields)
        val cartItemsJson = customFields["cartItems"]
        val items = if (cartItemsJson != null) {
            parseCartItems(cartItemsJson)
        } else {
            listOf(CartItemData(
                name = productName ?: "Service/Custom",
                price = transaction.unitPrice,
                quantity = transaction.quantity,
                subtotal = transaction.unitPrice * transaction.quantity
            ))
        }

        // Prefer order-time GST snapshot from customFields; fall back to live settings
        val snapshotRate = customFields["gstRate"]?.toIntOrNull()
        val effectiveGstRate = snapshotRate ?: gstRate
        val effectiveGstType = customFields["gstType"] ?: gstType
        val effectivePricingMode = customFields["gstPricingMode"] ?: gstPricingMode
        // Snapshot present ⇒ GST was active when the order was placed
        val hasGstin = businessGstin.isNotBlank() && (snapshotRate != null || gstEnabled)

        // Totals come from the same engine the checkout screen uses, so the PDF can
        // never disagree with what the user confirmed before saving the order.
        val discountType = customFields["discountType"] ?: "AMOUNT"
        val discountValue = customFields["discountValue"]?.toDoubleOrNull() ?: 0.0

        val pricingMode = TaxCalculator.pricingModeOf(effectivePricingMode)
        val gstTypeEnum = TaxCalculator.gstTypeOf(effectiveGstType)
        val breakdowns = items.map { item ->
            TaxCalculator.calculateLine(
                unitPrice = item.price,
                quantity = item.quantity,
                pricingMode = pricingMode,
                gstRate = effectiveGstRate,
                gstType = gstTypeEnum,
                hasGstin = hasGstin
            )
        }
        val orderTotals = TaxCalculator.calculateOrder(
            items = items.mapIndexed { index, item ->
                OrderItemDraft(
                    id = "invoice_$index",
                    productId = "",
                    productName = item.name,
                    quantity = item.quantity,
                    unitPrice = item.price
                ) to breakdowns[index]
            },
            discountType = discountType,
            discountValue = discountValue,
            pricingMode = pricingMode
        )

        val subtotal = orderTotals.subtotal
        val discountAmount = orderTotals.discountAmount
        val totalCgst = orderTotals.totalCgst
        val totalSgst = orderTotals.totalSgst
        val totalIgst = orderTotals.totalIgst
        val totalTax = orderTotals.totalTax
        val grandTotal = orderTotals.grandTotal
        val roundedGrandTotal = orderTotals.roundedGrandTotal
        val roundOffAmount = orderTotals.roundOffAmount

        // Percent labels come from the configured rate, not from reverse-calculating
        // tax / subtotal, which is wrong whenever a discount is applied.
        val halfRate = effectiveGstRate / 2
        val cgstPercent = if (effectiveGstType == "CGST_SGST") halfRate else 0
        val sgstPercent = if (effectiveGstType == "CGST_SGST") halfRate else 0
        val igstPercent = if (effectiveGstType == "IGST") effectiveGstRate else 0
        val referenceNumber = customFields["referenceNumber"] ?: ""

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 10f

        // --- Header Background ---
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 175f, bgPaint)

        // --- Business Name / Title ---
        canvas.drawText(businessName.ifEmpty { "MyManager" }, MARGIN, y + 20f, titlePaint)
        y += 40f

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
        if (businessWebsite.isNotBlank()) {
            canvas.drawText("Website: $businessWebsite", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (hasGstin && businessGstin.isNotBlank()) {
            canvas.drawText("GSTIN: $businessGstin", MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        if (businessPhone.isNotBlank() || businessEmail.isNotBlank() || businessAddress.isNotBlank() || businessWebsite.isNotBlank() || (hasGstin && businessGstin.isNotBlank())) {
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
        val yearMonth = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date(transaction.date))
        val seq = String.format(Locale.US, "%04d", kotlin.math.abs(transaction.id.hashCode() % 10000))
        val invoiceNumber = "$yearMonth-$seq"

        canvas.drawText("Date: $invoiceDate", MARGIN, y, bodyPaint)
        val invoiceText = "Invoice #: $invoiceNumber"
        val invoiceTextWidth = bodyPaint.measureText(invoiceText)
        canvas.drawText(invoiceText, PAGE_WIDTH - MARGIN - invoiceTextWidth, y, bodyPaint)

        if (transaction.orderId.isNotBlank()) {
            y += 16f
            val orderIdText = "Order ID: ${transaction.orderId}"
            val orderIdTextWidth = bodyPaint.measureText(orderIdText)
            canvas.drawText(orderIdText, PAGE_WIDTH - MARGIN - orderIdTextWidth, y, bodyPaint)
        }

        if (referenceNumber.isNotBlank()) {
            y += 16f
            val refText = "Ref: $referenceNumber"
            val refTextWidth = bodyPaint.measureText(refText)
            canvas.drawText(refText, PAGE_WIDTH - MARGIN - refTextWidth, y, bodyPaint)
        }

        y += 30f

        // --- Divider ---
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // --- Client Details ---
        canvas.drawText("BILL TO:", MARGIN, y, smallPaint)
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

        // --- Line Items Table ---
        val itemCol = MARGIN
        val qtyCol = MARGIN + 230f
        val priceCol = MARGIN + 300f
        val amtCol = PAGE_WIDTH - MARGIN

        // Table header background
        canvas.drawRect(MARGIN - 5f, y - 14f, PAGE_WIDTH - MARGIN + 5f, y + 6f, bgPaint)
        canvas.drawText("ITEM", itemCol, y, boldBodyPaint)
        canvas.drawText("QTY", qtyCol, y, boldBodyPaint)
        canvas.drawText("PRICE", priceCol, y, boldBodyPaint)
        val amtHeader = "AMOUNT"
        canvas.drawText(amtHeader, amtCol - boldBodyPaint.measureText(amtHeader), y, boldBodyPaint)
        y += LINE_HEIGHT + 5f

        if (cartItemsJson != null) {
            // Cart-based order: show each item as a row
            val cartItems = parseCartItems(cartItemsJson)
            if (cartItems.isNotEmpty()) {
                // Cart item rows
                for (item in cartItems) {
                    canvas.drawText(item.name, itemCol, y, bodyPaint)
                    canvas.drawText("${item.quantity}", qtyCol, y, bodyPaint)
                    val priceText = CurrencyUtils.formatCurrency(item.price, CurrencyUtils.loadCurrencyCode(context))
                    canvas.drawText(priceText, priceCol, y, bodyPaint)
                    val amtText = CurrencyUtils.formatCurrency(item.subtotal, CurrencyUtils.loadCurrencyCode(context))
                    canvas.drawText(amtText, amtCol - bodyPaint.measureText(amtText), y, bodyPaint)
                    y += LINE_HEIGHT
                }
            }
        } else {
            // Manual order: same ITEM | QTY | PRICE | AMOUNT format
            val displayName = productName ?: "Service/Custom"
            canvas.drawText(displayName, itemCol, y, bodyPaint)
            canvas.drawText("${transaction.quantity}", qtyCol, y, bodyPaint)
            val unitPriceText = CurrencyUtils.formatCurrency(transaction.unitPrice, CurrencyUtils.loadCurrencyCode(context))
            canvas.drawText(unitPriceText, priceCol, y, bodyPaint)
            val amtText = CurrencyUtils.formatCurrency(transaction.unitPrice * transaction.quantity, CurrencyUtils.loadCurrencyCode(context))
            canvas.drawText(amtText, amtCol - bodyPaint.measureText(amtText), y, bodyPaint)
            y += LINE_HEIGHT
        }

        // Subtotal line (raw line-item sum, not payable)
        y += 5f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 15f
        canvas.drawText("Subtotal", itemCol, y, boldBodyPaint)
        val subtotalText = CurrencyUtils.formatCurrency(subtotal, CurrencyUtils.loadCurrencyCode(context))
        canvas.drawText(subtotalText, amtCol - amountPaint.measureText(subtotalText), y, amountPaint)
        y += LINE_HEIGHT

        // Discount line (when applied)
        if (discountAmount > 0) {
            canvas.drawText(if (discountType == "PERCENT") "Discount (${discountValue.toInt()}%)" else "Discount", itemCol, y, bodyPaint)
            val discountText = "- " + CurrencyUtils.formatCurrency(discountAmount, CurrencyUtils.loadCurrencyCode(context))
            canvas.drawText(discountText, amtCol - bodyPaint.measureText(discountText), y, bodyPaint)
            y += LINE_HEIGHT
        }

        // Other custom fields (exclude internal/system keys)
        val internalKeys = setOf(
            "cartItems", "discountType", "discountValue",
            "gstRate", "gstType", "gstPricingMode", "referenceNumber"
        )
        val otherFields = customFields.filterKeys { it !in internalKeys }
        if (otherFields.isNotEmpty()) {
            y += 10f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
            y += 15f
            canvas.drawText("Additional Details", MARGIN, y, boldBodyPaint)
            y += LINE_HEIGHT
            for ((key, value) in otherFields) {
                canvas.drawText(key, MARGIN + 10f, y, smallPaint)
                canvas.drawText(value, MARGIN + 130f, y, bodyPaint)
                y += LINE_HEIGHT
            }
        }

        // --- Notes ---
        if (transaction.notes.isNotBlank()) {
            y += 5f
            canvas.drawText("Notes: ${transaction.notes}", MARGIN + 10f, y, smallPaint)
            y += LINE_HEIGHT
        }

        y += 10f

        // --- Divider ---
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 20f

        // --- Totals Section ---
        val totalsX = PAGE_WIDTH - MARGIN - 200f
        val rightEdge = PAGE_WIDTH - MARGIN

        // Payment Method on LEFT side (aligned with Total Amount)
        var statusBadgeY = -1f
        if (transaction.paymentMethod.isNotBlank()) {
            canvas.drawText("Payment Method:", MARGIN, y, bodyPaint)
            canvas.drawText(transaction.paymentMethod, MARGIN, y + LINE_HEIGHT, boldBodyPaint)
            statusBadgeY = y + LINE_HEIGHT + 28f
        } else {
            statusBadgeY = y + 5f
        }

        // --- Status Badge (left side, below Payment Method) ---
        val statusText = when (transaction.status) {
            "COMPLETED" -> "PAID"
            "PARTIAL" -> "PARTIALLY PAID"
            else -> "PAYMENT PENDING"
        }
        val statusPaint = Paint(boldBodyPaint).apply {
            color = when (transaction.status) {
                "COMPLETED" -> android.graphics.Color.parseColor("#388E3C")
                "PARTIAL" -> android.graphics.Color.parseColor("#F57C00")
                else -> android.graphics.Color.parseColor("#D32F2F")
            }
            textSize = 16f
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
        val statusX = MARGIN
        canvas.drawRect(statusX, statusBadgeY - 16f, statusX + statusWidth + 10f, statusBadgeY + 6f, statusBgPaint)
        canvas.drawText(statusText, statusX, statusBadgeY, statusPaint)

        canvas.drawText("Total Amount:", totalsX, y, boldBodyPaint)
        val totalText = CurrencyUtils.formatCurrency(grandTotal, CurrencyUtils.loadCurrencyCode(context))
        val totalWidth = amountPaint.measureText(totalText)
        canvas.drawText(totalText, rightEdge - totalWidth, y, amountPaint)
        y += LINE_HEIGHT + 5f

        // GST Breakdown
        if (hasGstin && totalTax > 0) {
            if (effectiveGstType == "CGST_SGST") {
                canvas.drawText("CGST (${cgstPercent}%)", totalsX, y, bodyPaint)
                val cgstText = CurrencyUtils.formatCurrency(totalCgst, CurrencyUtils.loadCurrencyCode(context))
                val cgstWidth = bodyPaint.measureText(cgstText)
                canvas.drawText(cgstText, rightEdge - cgstWidth, y, bodyPaint)
                y += LINE_HEIGHT + 5f

                canvas.drawText("SGST (${sgstPercent}%)", totalsX, y, bodyPaint)
                val sgstText = CurrencyUtils.formatCurrency(totalSgst, CurrencyUtils.loadCurrencyCode(context))
                val sgstWidth = bodyPaint.measureText(sgstText)
                canvas.drawText(sgstText, rightEdge - sgstWidth, y, bodyPaint)
                y += LINE_HEIGHT + 5f
            } else {
                canvas.drawText("IGST (${igstPercent}%)", totalsX, y, bodyPaint)
                val igstText = CurrencyUtils.formatCurrency(totalIgst, CurrencyUtils.loadCurrencyCode(context))
                val igstWidth = bodyPaint.measureText(igstText)
                canvas.drawText(igstText, rightEdge - igstWidth, y, bodyPaint)
                y += LINE_HEIGHT + 5f
            }

            canvas.drawText("Total Tax", totalsX, y, boldBodyPaint)
            val taxText = CurrencyUtils.formatCurrency(totalTax, CurrencyUtils.loadCurrencyCode(context))
            val taxWidth = amountPaint.measureText(taxText)
            canvas.drawText(taxText, rightEdge - taxWidth, y, amountPaint)
            y += LINE_HEIGHT + 5f
        }

        canvas.drawText("Amount Paid:", totalsX, y, bodyPaint)
        val paidText = CurrencyUtils.formatCurrency(transaction.paidAmount, CurrencyUtils.loadCurrencyCode(context))
        val paidWidth = bodyPaint.measureText(paidText)
        canvas.drawText(paidText, rightEdge - paidWidth, y, bodyPaint)
        y += LINE_HEIGHT + 5f

        // Round-off
        if (roundOffAmount != 0.0) {
            canvas.drawText("Round Off:", totalsX, y, bodyPaint)
            val roundOffText = CurrencyUtils.formatCurrency(roundOffAmount, CurrencyUtils.loadCurrencyCode(context))
            val roundOffWidth = bodyPaint.measureText(roundOffText)
            canvas.drawText(roundOffText, rightEdge - roundOffWidth, y, bodyPaint)
            y += LINE_HEIGHT + 5f
        }

        canvas.drawText("Payable Amount:", totalsX, y, boldBodyPaint)
        val payableText = CurrencyUtils.formatCurrency(roundedGrandTotal.toDouble(), CurrencyUtils.loadCurrencyCode(context))
        val payableWidth = amountPaint.measureText(payableText)
        canvas.drawText(payableText, rightEdge - payableWidth, y, amountPaint)
        y += LINE_HEIGHT + 5f

        val balance = roundedGrandTotal.toDouble() - transaction.paidAmount
        val balancePaint = if (balance > 0) Paint(amountPaint).apply {
            color = android.graphics.Color.parseColor("#D32F2F")
        } else amountPaint

        canvas.drawText("Balance Due:", totalsX, y, boldBodyPaint)
        val balanceText = CurrencyUtils.formatCurrency(balance, CurrencyUtils.loadCurrencyCode(context))
        val balanceWidth = balancePaint.measureText(balanceText)
        canvas.drawText(balanceText, rightEdge - balanceWidth, y, balancePaint)
        y += LINE_HEIGHT + 15f

        // --- Footer ---
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 60f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 60f, linePaint)
        val footerText = "Generated by MyManager"
        val footerWidth = smallPaint.measureText(footerText)
        canvas.drawText(
            footerText,
            (PAGE_WIDTH - footerWidth) / 2f,
            PAGE_HEIGHT - 40f,
            smallPaint
        )

        document.finishPage(page)

        val fileName = "MyManager_Invoice_${invoiceNumber}_${System.currentTimeMillis()}.pdf"

        return try {
            // Save to cache for opening/previewing
            val cacheDir = File(context.cacheDir, "shared_invoices")
            cacheDir.mkdirs()
            val cacheFile = File(cacheDir, fileName)
            FileOutputStream(cacheFile).use { outputStream ->
                document.writeTo(outputStream)
            }

            val cacheUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            // Save to Downloads (reuse document bytes from cache file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val downloadsUri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                downloadsUri?.let { uri ->
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        cacheFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                cacheFile.copyTo(file, overwrite = true)
            }

            document.close()
            InvoiceResult(fileName, cacheUri)
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    // Helper data class for cart items
    data class CartItemData(
        val name: String,
        val price: Double,
        val quantity: Int,
        val subtotal: Double
    )

    // Helper function to parse custom fields JSON
    fun parseCustomFields(json: String): Map<String, String> {
        return try {
            Json.decodeFromString<Map<String, String>>(json)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Helper function to parse cart items JSON
    fun parseCartItems(json: String): List<CartItemData> {
        return try {
            val items = Json.decodeFromString<List<Map<String, String>>>(json)
            items.mapNotNull { item ->
                val name = item["productName"] ?: return@mapNotNull null
                val price = item["price"]?.toDoubleOrNull() ?: 0.0
                val quantity = item["quantity"]?.toIntOrNull() ?: 1
                val subtotal = item["subtotal"]?.toDoubleOrNull() ?: (price * quantity)
                CartItemData(name, price, quantity, subtotal)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}