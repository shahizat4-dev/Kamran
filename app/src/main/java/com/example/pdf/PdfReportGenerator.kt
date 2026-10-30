package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.TruckDeliveryEntity
import com.example.model.UnitConversions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points (72 dpi)
    private const val MARGIN = 36f

    fun generateDeliveryReport(
        context: Context,
        projectName: String,
        siteLocation: String,
        reportSubtitle: String,
        deliveries: List<TruckDeliveryEntity>
    ): File {
        val pdfDocument = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42) // Slate 900
            textSize = 10f
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(217, 119, 6) // AmberPrimary
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subheaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105) // Slate 600
            textSize = 9f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240) // Slate 200
            strokeWidth = 1f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN

        // --- HEADER BANNER ---
        fillPaint.color = Color.rgb(30, 41, 59) // Slate 800
        canvas.drawRoundRect(RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 65f), 6f, 6f, fillPaint)

        // Title in Banner
        headerPaint.color = Color.rgb(245, 158, 11) // Amber
        canvas.drawText("CONSTRUCTION MATERIAL & DELIVERY REPORT", MARGIN + 14f, y + 26f, headerPaint)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US)
        val shortDateFormat = SimpleDateFormat("MM/dd/yy", Locale.US)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            textSize = 8.5f
        }
        canvas.drawText("Project: $projectName | Site: $siteLocation", MARGIN + 14f, y + 42f, metaPaint)
        canvas.drawText("Scope: $reportSubtitle | Generated: ${dateFormat.format(Date())}", MARGIN + 14f, y + 54f, metaPaint)

        y += 78f

        // --- EXECUTIVE SUMMARY KPI CARDS ---
        val totalCft = deliveries.sumOf { it.totalCft }
        val totalBrass = deliveries.sumOf { it.totalBrass }
        val totalMaterialCost = deliveries.sumOf { it.materialCost }
        val totalExpenses = deliveries.sumOf { it.totalExpenses }
        val grandTotal = deliveries.sumOf { it.grandTotal }
        val totalDeliveries = deliveries.size

        val cardWidth = (PAGE_WIDTH - (MARGIN * 2) - 18f) / 4f
        val cardHeight = 44f

        drawKpiCard(canvas, MARGIN, y, cardWidth, cardHeight, "Total CFT Delivered", "${UnitConversions.formatNumber(totalCft, 1)} CFT", "(${UnitConversions.formatNumber(totalBrass, 2)} Brass)", Color.rgb(2, 132, 199))
        drawKpiCard(canvas, MARGIN + cardWidth + 6f, y, cardWidth, cardHeight, "Total Deliveries", "$totalDeliveries Trucks", "Logged Trips", Color.rgb(100, 116, 139))
        drawKpiCard(canvas, MARGIN + (cardWidth + 6f) * 2, y, cardWidth, cardHeight, "Material Cost", UnitConversions.formatCurrency(totalMaterialCost), "Base Value", Color.rgb(217, 119, 6))
        drawKpiCard(canvas, MARGIN + (cardWidth + 6f) * 3, y, cardWidth, cardHeight, "Grand Total (Site)", UnitConversions.formatCurrency(grandTotal), "Incl. Expenses", Color.rgb(5, 150, 105))

        y += cardHeight + 16f

        // --- DELIVERIES TABLE ---
        boldPaint.textSize = 11f
        canvas.drawText("Detailed Daily Truck Delivery Log", MARGIN, y, boldPaint)
        y += 12f

        // Table Header
        fillPaint.color = Color.rgb(241, 245, 249) // Slate 100
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, fillPaint)
        linePaint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(MARGIN, y + 20f, PAGE_WIDTH - MARGIN, y + 20f, linePaint)

        boldPaint.textSize = 7.5f
        boldPaint.color = Color.rgb(71, 85, 105)

        val colX = floatArrayOf(
            MARGIN + 4f,        // Date (0)
            MARGIN + 48f,       // Truck # (1)
            MARGIN + 104f,      // Challan (2)
            MARGIN + 152f,      // Material (3)
            MARGIN + 248f,      // CFT (4)
            MARGIN + 296f,      // Brass (5)
            MARGIN + 338f,      // Rate/CFT (6)
            MARGIN + 382f,      // Mat Cost (7)
            MARGIN + 434f,      // Expenses (8)
            MARGIN + 482f       // Grand Total (9)
        )

        canvas.drawText("DATE", colX[0], y + 13f, boldPaint)
        canvas.drawText("TRUCK #", colX[1], y + 13f, boldPaint)
        canvas.drawText("CHALLAN", colX[2], y + 13f, boldPaint)
        canvas.drawText("MATERIAL", colX[3], y + 13f, boldPaint)
        canvas.drawText("CFT", colX[4], y + 13f, boldPaint)
        canvas.drawText("BRASS", colX[5], y + 13f, boldPaint)
        canvas.drawText("RATE", colX[6], y + 13f, boldPaint)
        canvas.drawText("MAT COST", colX[7], y + 13f, boldPaint)
        canvas.drawText("EXPENSES", colX[8], y + 13f, boldPaint)
        canvas.drawText("TOTAL", colX[9], y + 13f, boldPaint)

        y += 20f

        textPaint.textSize = 7.5f

        for (item in deliveries) {
            // Check if page full, trigger next page
            if (y > PAGE_HEIGHT - 120f) {
                // Page footer
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN

                // Repeat minimal table header on continuation
                boldPaint.textSize = 9f
                canvas.drawText("Delivery Log (Continued - Page $pageNumber)", MARGIN, y + 10f, boldPaint)
                y += 18f

                fillPaint.color = Color.rgb(241, 245, 249)
                canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, fillPaint)
                boldPaint.textSize = 7.5f
                boldPaint.color = Color.rgb(71, 85, 105)
                canvas.drawText("DATE", colX[0], y + 12f, boldPaint)
                canvas.drawText("TRUCK #", colX[1], y + 12f, boldPaint)
                canvas.drawText("CHALLAN", colX[2], y + 12f, boldPaint)
                canvas.drawText("MATERIAL", colX[3], y + 12f, boldPaint)
                canvas.drawText("CFT", colX[4], y + 12f, boldPaint)
                canvas.drawText("BRASS", colX[5], y + 12f, boldPaint)
                canvas.drawText("RATE", colX[6], y + 12f, boldPaint)
                canvas.drawText("MAT COST", colX[7], y + 12f, boldPaint)
                canvas.drawText("EXPENSES", colX[8], y + 12f, boldPaint)
                canvas.drawText("TOTAL", colX[9], y + 12f, boldPaint)
                y += 18f
            }

            val dateStr = shortDateFormat.format(Date(item.dateEpochMs))
            textPaint.color = Color.rgb(30, 41, 59)

            canvas.drawText(dateStr, colX[0], y + 12f, textPaint)
            canvas.drawText(truncate(item.truckNumber, 10), colX[1], y + 12f, textPaint)
            canvas.drawText(truncate(item.challanNumber.ifEmpty { "-" }, 9), colX[2], y + 12f, textPaint)
            canvas.drawText(truncate(item.materialName, 17), colX[3], y + 12f, textPaint)
            canvas.drawText(UnitConversions.formatNumber(item.totalCft, 1), colX[4], y + 12f, textPaint)
            canvas.drawText(UnitConversions.formatNumber(item.totalBrass, 2), colX[5], y + 12f, textPaint)
            canvas.drawText("$${UnitConversions.formatNumber(item.ratePerCft, 1)}", colX[6], y + 12f, textPaint)
            canvas.drawText("$${UnitConversions.formatNumber(item.materialCost, 0)}", colX[7], y + 12f, textPaint)
            canvas.drawText("$${UnitConversions.formatNumber(item.totalExpenses, 0)}", colX[8], y + 12f, textPaint)

            boldPaint.textSize = 7.5f
            boldPaint.color = Color.rgb(15, 23, 42)
            canvas.drawText("$${UnitConversions.formatNumber(item.grandTotal, 0)}", colX[9], y + 12f, boldPaint)

            linePaint.color = Color.rgb(241, 245, 249)
            canvas.drawLine(MARGIN, y + 16f, PAGE_WIDTH - MARGIN, y + 16f, linePaint)
            y += 17f
        }

        // Totals Row
        fillPaint.color = Color.rgb(254, 243, 199) // Amber Container
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 18f, fillPaint)
        boldPaint.textSize = 8f
        boldPaint.color = Color.rgb(120, 53, 15)
        canvas.drawText("TOTALS", colX[0], y + 12f, boldPaint)
        canvas.drawText("${UnitConversions.formatNumber(totalCft, 1)}", colX[4], y + 12f, boldPaint)
        canvas.drawText("${UnitConversions.formatNumber(totalBrass, 2)}", colX[5], y + 12f, boldPaint)
        canvas.drawText("$${UnitConversions.formatNumber(totalMaterialCost, 0)}", colX[7], y + 12f, boldPaint)
        canvas.drawText("$${UnitConversions.formatNumber(totalExpenses, 0)}", colX[8], y + 12f, boldPaint)
        canvas.drawText("$${UnitConversions.formatNumber(grandTotal, 0)}", colX[9], y + 12f, boldPaint)
        y += 26f

        // Check if room for summary breakdown
        if (y > PAGE_HEIGHT - 170f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = MARGIN
        }

        // --- MATERIAL SUMMARY & EXPENSE BREAKDOWN SIDE-BY-SIDE ---
        val sectionWidth = (PAGE_WIDTH - (MARGIN * 2) - 16f) / 2f

        // Material Summary Box
        boldPaint.textSize = 10f
        boldPaint.color = Color.rgb(15, 23, 42)
        canvas.drawText("Material-Wise Consumption", MARGIN, y + 10f, boldPaint)

        // Expense Breakdown Box
        canvas.drawText("Manual Transport & Site Expenses", MARGIN + sectionWidth + 16f, y + 10f, boldPaint)
        y += 16f

        // Material table
        val materialMap = deliveries.groupBy { it.materialName }
        var matY = y
        fillPaint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(MARGIN, matY, MARGIN + sectionWidth, matY + 14f, fillPaint)
        boldPaint.textSize = 7f
        boldPaint.color = Color.rgb(71, 85, 105)
        canvas.drawText("MATERIAL", MARGIN + 4f, matY + 10f, boldPaint)
        canvas.drawText("CFT", MARGIN + sectionWidth - 80f, matY + 10f, boldPaint)
        canvas.drawText("COST", MARGIN + sectionWidth - 36f, matY + 10f, boldPaint)
        matY += 15f

        textPaint.textSize = 7f
        for ((mat, list) in materialMap.entries.take(5)) {
            val matCft = list.sumOf { it.totalCft }
            val matCost = list.sumOf { it.materialCost }
            canvas.drawText(truncate(mat, 18), MARGIN + 4f, matY + 10f, textPaint)
            canvas.drawText(UnitConversions.formatNumber(matCft, 1), MARGIN + sectionWidth - 80f, matY + 10f, textPaint)
            canvas.drawText("$${UnitConversions.formatNumber(matCost, 0)}", MARGIN + sectionWidth - 36f, matY + 10f, textPaint)
            matY += 13f
        }

        // Expenses breakdown table
        var expY = y
        canvas.drawRect(MARGIN + sectionWidth + 16f, expY, PAGE_WIDTH - MARGIN, expY + 14f, fillPaint)
        canvas.drawText("CATEGORY", MARGIN + sectionWidth + 20f, expY + 10f, boldPaint)
        canvas.drawText("AMOUNT", PAGE_WIDTH - MARGIN - 45f, expY + 10f, boldPaint)
        expY += 15f

        val totalDiesel = deliveries.sumOf { it.dieselExpense }
        val totalToll = deliveries.sumOf { it.tollExpense }
        val totalLabor = deliveries.sumOf { it.unloadingLabor }
        val totalBatta = deliveries.sumOf { it.driverAllowance }
        val totalWeigh = deliveries.sumOf { it.weighbridgeFee }
        val totalOther = deliveries.sumOf { it.otherExpense }

        val expenseItems = listOf(
            Pair("Diesel / Fuel", totalDiesel),
            Pair("Toll Charges", totalToll),
            Pair("Unloading Labor (Hamali)", totalLabor),
            Pair("Driver Batta / Allowance", totalBatta),
            Pair("Weighbridge & Misc", totalWeigh + totalOther)
        )

        for ((label, amount) in expenseItems) {
            canvas.drawText(label, MARGIN + sectionWidth + 20f, expY + 10f, textPaint)
            canvas.drawText("$${UnitConversions.formatNumber(amount, 0)}", PAGE_WIDTH - MARGIN - 45f, expY + 10f, textPaint)
            expY += 13f
        }

        y = maxOf(matY, expY) + 20f

        // --- SIGN OFF BLOCK ---
        if (y > PAGE_HEIGHT - 80f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(page)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            y = MARGIN + 20f
        }

        linePaint.color = Color.rgb(100, 116, 139)
        linePaint.strokeWidth = 0.8f

        // Sign 1
        canvas.drawLine(MARGIN + 20f, y + 25f, MARGIN + 180f, y + 25f, linePaint)
        subheaderPaint.textSize = 8f
        canvas.drawText("Prepared By: Site Supervisor / Eng.", MARGIN + 20f, y + 36f, subheaderPaint)

        // Sign 2
        canvas.drawLine(PAGE_WIDTH - MARGIN - 180f, y + 25f, PAGE_WIDTH - MARGIN - 20f, y + 25f, linePaint)
        canvas.drawText("Approved By: Project Manager", PAGE_WIDTH - MARGIN - 180f, y + 36f, subheaderPaint)

        drawPageFooter(canvas, pageNumber)
        pdfDocument.finishPage(page)

        // Save PDF to cache
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "Delivery_Report_${System.currentTimeMillis()}.pdf"
        val outputFile = File(reportsDir, fileName)
        FileOutputStream(outputFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return outputFile
    }

    private fun drawKpiCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        subtext: String,
        accentColor: Int
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(248, 250, 252)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val accentBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
        }

        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 4f, 4f, bgPaint)
        canvas.drawRoundRect(RectF(x, y, x + width, y + height), 4f, 4f, borderPaint)
        // Left accent bar
        canvas.drawRoundRect(RectF(x, y, x + 3.5f, y + height), 2f, 2f, accentBarPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 7f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 6.5f
        }

        canvas.drawText(label, x + 8f, y + 13f, labelPaint)
        canvas.drawText(value, x + 8f, y + 27f, valuePaint)
        canvas.drawText(subtext, x + 8f, y + 38f, subPaint)
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN, PAGE_HEIGHT - 24f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 24f, linePaint)
        canvas.drawText("Generated with CFT Tracker for Project Managers", MARGIN, PAGE_HEIGHT - 12f, footerPaint)
        canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN - 32f, PAGE_HEIGHT - 12f, footerPaint)
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 2) + ".." else text
    }

    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Construction Delivery & CFT Report")
            putExtra(Intent.EXTRA_TEXT, "Attached is the latest Construction Material & Delivery PDF Report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createViewIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
