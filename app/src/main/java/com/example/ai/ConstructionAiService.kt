package com.example.ai

import android.util.Log
import com.example.data.local.TruckDeliveryEntity
import com.example.model.UnitConversions
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "ConstructionAiService"

data class ParsedDeliverySlip(
    val truckNumber: String = "",
    val challanNumber: String = "",
    val materialName: String = "",
    val lengthFt: Double = 0.0,
    val lengthIn: Double = 0.0,
    val widthFt: Double = 0.0,
    val widthIn: Double = 0.0,
    val heightFt: Double = 0.0,
    val heightIn: Double = 0.0,
    val ratePerCft: Double = 0.0,
    val dieselExpense: Double = 0.0,
    val tollExpense: Double = 0.0,
    val driverAllowance: Double = 0.0,
    val unloadingLabor: Double = 0.0,
    val notes: String = ""
)

sealed interface AiResultState<out T> {
    data object Idle : AiResultState<Nothing>
    data object Loading : AiResultState<Nothing>
    data class Streaming(val partialText: String) : AiResultState<Nothing>
    data class Success<T>(val data: T) : AiResultState<T>
    data class Error(val message: String) : AiResultState<Nothing>
}

class ConstructionAiService {

    // General purpose Gemini 3.8 Flash model
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    // Structured JSON Schema for parsing delivery challans
    private val slipSchema = Schema.obj(
        properties = mapOf(
            "truckNumber" to Schema.string(description = "Truck registration or vehicle number"),
            "challanNumber" to Schema.string(description = "Delivery slip or invoice number"),
            "materialName" to Schema.string(description = "Construction material name (e.g. River Sand, 20mm Aggregates, Crushed Stone)"),
            "lengthFeet" to Schema.double(description = "Length in feet"),
            "lengthInches" to Schema.double(description = "Length additional inches"),
            "widthFeet" to Schema.double(description = "Width in feet"),
            "widthInches" to Schema.double(description = "Width additional inches"),
            "heightFeet" to Schema.double(description = "Height in feet"),
            "heightInches" to Schema.double(description = "Height additional inches"),
            "ratePerCft" to Schema.double(description = "Rate per cubic foot"),
            "dieselExpense" to Schema.double(description = "Diesel or fuel cost"),
            "tollExpense" to Schema.double(description = "Toll gate charges"),
            "driverAllowance" to Schema.double(description = "Driver batta or allowance"),
            "unloadingLabor" to Schema.double(description = "Unloading or hamali labor fee"),
            "summaryNotes" to Schema.string(description = "Brief summary notes or supplier name")
        ),
        optionalProperties = listOf(
            "challanNumber", "ratePerCft", "dieselExpense", "tollExpense",
            "driverAllowance", "unloadingLabor", "summaryNotes"
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = slipSchema
            }
        )
    }

    /**
     * Estimates construction material quantities (sand, aggregate, cement, CFT, truckloads)
     * using streaming Gemini 3.8 Flash response.
     */
    fun estimateMaterialRequirements(projectPrompt: String): Flow<String> = flow {
        val systemContext = """
            You are a civil engineering expert and construction quantity surveyor.
            Analyze the following project description, compute total volume in Cubic Feet (CFT) and Brass (100 CFT = 1 Brass),
            apply standard dry volume shrinkage factors (e.g. +54% for concrete mixes),
            breakdown requirements into:
            1. Total Volume in CFT and Brass
            2. Material breakdown (Cubic Feet of Sand, Aggregates, Cement bags, etc.)
            3. Estimated number of 10-wheeler dump trucks (approx 500 CFT each) or 6-wheeler tippers (approx 350 CFT each)
            4. Practical site tips and wastage allowance (+5-10%).
            Format cleanly with clear headings and bullet points.
        """.trimIndent()

        val fullPrompt = "$systemContext\n\nProject Details:\n$projectPrompt"

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            generalModel.generateContentStream(fullPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                accumulated.append(text)
                emit(accumulated.toString())
            }
        }
    }

    /**
     * Parses free-form text from delivery challan / gate pass into structured fields.
     */
    suspend fun parseChallanText(rawText: String): ParsedDeliverySlip {
        val prompt = "Extract construction delivery slip information from this text into JSON:\n$rawText"

        val response = withTimeout(60_000L) {
            structuredModel.generateContent(prompt)
        }

        val jsonText = response.text ?: throw IllegalStateException("Model returned empty text")
        val json = JSONObject(jsonText)

        return ParsedDeliverySlip(
            truckNumber = json.optString("truckNumber", ""),
            challanNumber = json.optString("challanNumber", ""),
            materialName = json.optString("materialName", "River Sand"),
            lengthFt = json.optDouble("lengthFeet", 16.0),
            lengthIn = json.optDouble("lengthInches", 0.0),
            widthFt = json.optDouble("widthFeet", 7.0),
            widthIn = json.optDouble("widthInches", 6.0),
            heightFt = json.optDouble("heightFeet", 4.0),
            heightIn = json.optDouble("heightInches", 0.0),
            ratePerCft = json.optDouble("ratePerCft", 45.0),
            dieselExpense = json.optDouble("dieselExpense", 0.0),
            tollExpense = json.optDouble("tollExpense", 0.0),
            driverAllowance = json.optDouble("driverAllowance", 0.0),
            unloadingLabor = json.optDouble("unloadingLabor", 0.0),
            notes = json.optString("summaryNotes", "")
        )
    }

    /**
     * Project Manager Site Audit: Analyzes recent delivery logs and expenses.
     */
    suspend fun auditSiteLogistics(deliveries: List<TruckDeliveryEntity>): String {
        val deliverySummary = buildString {
            append("Site Delivery Records (${deliveries.size} trips):\n")
            deliveries.take(15).forEach { d ->
                append("• ${d.truckNumber} | ${d.materialName} | ${UnitConversions.formatNumber(d.totalCft, 1)} CFT | Mat: $${d.materialCost} | Diesel: $${d.dieselExpense} | Toll: $${d.tollExpense} | Unloading: $${d.unloadingLabor} | Status: ${d.paymentStatus}\n")
            }
        }

        val prompt = """
            You are a senior construction project manager auditor.
            Review these daily truck deliveries and manual expenses:
            $deliverySummary

            Provide a concise, professional executive briefing:
            1. Total Material Volume & Delivery Efficiency
            2. Transport & Manual Expense Observations (Diesel, Toll, Labor anomalies)
            3. Recommended actionable cost savings and site logistics optimizations.
        """.trimIndent()

        val response = withTimeout(60_000L) {
            generalModel.generateContent(prompt)
        }

        return response.text ?: "No audit generated."
    }
}
