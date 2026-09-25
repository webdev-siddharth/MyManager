package com.core2studio.mymanager.utils

object TaxCalculator {
    enum class PricingMode { INCLUSIVE, EXCLUSIVE }
    enum class GstType { CGST_SGST, IGST }

    /**
     * Safe enum parsing. The raw values come from SharedPreferences/Firestore, so a
     * corrupted or unknown value must not throw IllegalArgumentException inside
     * composition or a coroutine.
     */
    fun pricingModeOf(name: String?): PricingMode =
        PricingMode.entries.firstOrNull { it.name == name } ?: PricingMode.INCLUSIVE

    fun gstTypeOf(name: String?): GstType =
        GstType.entries.firstOrNull { it.name == name } ?: GstType.CGST_SGST

    data class LineTaxBreakdown(
        val baseAmount: Double,
        val taxableAmount: Double,
        val cgstRate: Double,
        val sgstRate: Double,
        val igstRate: Double,
        val cgstAmount: Double,
        val sgstAmount: Double,
        val igstAmount: Double,
        val totalTax: Double,
        val lineTotal: Double
    ) {
        val cgstPercent: String get() = "%.2f".format(cgstRate)
        val sgstPercent: String get() = "%.2f".format(sgstRate)
        val igstPercent: String get() = "%.2f".format(igstRate)
    }

    data class OrderTotals(
        val subtotal: Double,
        val discountAmount: Double,
        val taxableAmount: Double,
        val totalCgst: Double,
        val totalSgst: Double,
        val totalIgst: Double,
        val totalTax: Double,
        val grandTotal: Double,
        val roundedGrandTotal: Long,
        val roundOffAmount: Double
    )

    fun calculateLine(
        unitPrice: Double,
        quantity: Int = 1,
        pricingMode: PricingMode,
        gstRate: Int,
        gstType: GstType,
        hasGstin: Boolean
    ): LineTaxBreakdown {
        if (!hasGstin || gstRate <= 0) {
            val total = unitPrice * quantity
            return LineTaxBreakdown(
                baseAmount = total,
                taxableAmount = total,
                cgstRate = 0.0, sgstRate = 0.0, igstRate = 0.0,
                cgstAmount = 0.0, sgstAmount = 0.0, igstAmount = 0.0,
                totalTax = 0.0,
                lineTotal = total
            )
        }

        val lineSubtotal = unitPrice * quantity
        val rateDecimal = gstRate / 100.0

        return when (pricingMode) {
            PricingMode.INCLUSIVE -> {
                val taxableAmount = round2(lineSubtotal / (1 + rateDecimal))
                val totalTax = lineSubtotal - taxableAmount

                when (gstType) {
                    GstType.CGST_SGST -> {
                        val eachRate = gstRate / 2.0
                        val eachAmount = round2(totalTax / 2)
                        val cgstAmount = eachAmount
                        val sgstAmount = round2(totalTax - cgstAmount)
                        LineTaxBreakdown(
                            baseAmount = unitPrice,
                            taxableAmount = taxableAmount,
                            cgstRate = eachRate, sgstRate = eachRate, igstRate = 0.0,
                            cgstAmount = cgstAmount, sgstAmount = sgstAmount, igstAmount = 0.0,
                            totalTax = round2(cgstAmount + sgstAmount),
                            lineTotal = lineSubtotal
                        )
                    }
                    GstType.IGST -> {
                        val igstAmount = round2(totalTax)
                        LineTaxBreakdown(
                            baseAmount = unitPrice,
                            taxableAmount = taxableAmount,
                            cgstRate = 0.0, sgstRate = 0.0, igstRate = gstRate.toDouble(),
                            cgstAmount = 0.0, sgstAmount = 0.0, igstAmount = igstAmount,
                            totalTax = igstAmount,
                            lineTotal = lineSubtotal
                        )
                    }
                }
            }
            PricingMode.EXCLUSIVE -> {
                val taxableAmount = lineSubtotal
                val totalTax = round2(taxableAmount * rateDecimal)

                when (gstType) {
                    GstType.CGST_SGST -> {
                        val eachRate = gstRate / 2.0
                        val eachAmount = round2(totalTax / 2)
                        val cgstAmount = eachAmount
                        val sgstAmount = round2(totalTax - cgstAmount)
                        LineTaxBreakdown(
                            baseAmount = unitPrice,
                            taxableAmount = taxableAmount,
                            cgstRate = eachRate, sgstRate = eachRate, igstRate = 0.0,
                            cgstAmount = cgstAmount, sgstAmount = sgstAmount, igstAmount = 0.0,
                            totalTax = round2(cgstAmount + sgstAmount),
                            lineTotal = round2(taxableAmount + totalTax)
                        )
                    }
                    GstType.IGST -> {
                        val igstAmount = round2(totalTax)
                        LineTaxBreakdown(
                            baseAmount = unitPrice,
                            taxableAmount = taxableAmount,
                            cgstRate = 0.0, sgstRate = 0.0, igstRate = gstRate.toDouble(),
                            cgstAmount = 0.0, sgstAmount = 0.0, igstAmount = igstAmount,
                            totalTax = igstAmount,
                            lineTotal = round2(taxableAmount + totalTax)
                        )
                    }
                }
            }
        }
    }

    /**
     * Order-level totals.
     *
     * The discount is applied to the price the customer actually sees when GST is
     * already included in the line prices (INCLUSIVE), and to the pre-tax value when
     * it is not (EXCLUSIVE). This keeps `Subtotal - Discount = Grand Total` true on
     * screen and on the invoice, instead of discounting the net value and then
     * charging GST on top of a number the user never saw.
     */
    fun calculateOrder(
        items: List<Pair<com.core2studio.mymanager.data.local.entity.OrderItemDraft, LineTaxBreakdown>>,
        discountType: String,
        discountValue: Double,
        pricingMode: PricingMode
    ): OrderTotals {
        if (items.isEmpty()) {
            return OrderTotals(
                subtotal = 0.0, discountAmount = 0.0, taxableAmount = 0.0,
                totalCgst = 0.0, totalSgst = 0.0, totalIgst = 0.0, totalTax = 0.0,
                grandTotal = 0.0, roundedGrandTotal = 0L, roundOffAmount = 0.0
            )
        }

        // Base the discount is taken from
        val subtotal = if (pricingMode == PricingMode.INCLUSIVE) {
            items.sumOf { it.second.lineTotal }
        } else {
            items.sumOf { it.second.taxableAmount }
        }

        var totalCgst = items.sumOf { it.second.cgstAmount }
        var totalSgst = items.sumOf { it.second.sgstAmount }
        var totalIgst = items.sumOf { it.second.igstAmount }

        val rawDiscount = if (discountType == "PERCENT")
            subtotal * discountValue / 100.0 else discountValue
        val discountAmount = round2(rawDiscount).coerceIn(0.0, subtotal)
        val discountedBase = round2(subtotal - discountAmount)

        // Discount reduces taxable value (and therefore the tax) proportionally.
        val taxReductionFactor = if (subtotal > 0) discountedBase / subtotal else 1.0
        totalCgst = round2(totalCgst * taxReductionFactor)
        totalSgst = round2(totalSgst * taxReductionFactor)
        totalIgst = round2(totalIgst * taxReductionFactor)
        val totalTax = round2(totalCgst + totalSgst + totalIgst)

        // INCLUSIVE: the discounted price already contains tax, so the payable amount
        // is the discounted base itself. EXCLUSIVE: tax is added on top.
        val taxableAmount = if (pricingMode == PricingMode.INCLUSIVE)
            round2(discountedBase - totalTax) else discountedBase
        val grandTotal = if (pricingMode == PricingMode.INCLUSIVE)
            discountedBase else round2(discountedBase + totalTax)

        val roundedGrandTotal = Math.round(grandTotal)
        val roundOffAmount = roundedGrandTotal - grandTotal

        return OrderTotals(
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxableAmount = taxableAmount,
            totalCgst = totalCgst,
            totalSgst = totalSgst,
            totalIgst = totalIgst,
            totalTax = totalTax,
            grandTotal = grandTotal,
            roundedGrandTotal = roundedGrandTotal,
            roundOffAmount = roundOffAmount
        )
    }

    private fun round2(value: Double): Double = kotlin.math.round(value * 100.0) / 100.0
}