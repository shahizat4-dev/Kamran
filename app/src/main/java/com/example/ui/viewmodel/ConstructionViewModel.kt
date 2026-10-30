package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConstructionRepository
import com.example.data.local.MaterialEntity
import com.example.data.local.TruckDeliveryEntity
import com.example.model.UnitConversions
import com.example.pdf.PdfReportGenerator
import com.example.ai.AiResultState
import com.example.ai.ConstructionAiService
import com.example.ai.ParsedDeliverySlip
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

enum class DateFilter(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    LAST_7_DAYS("7 Days"),
    THIS_MONTH("This Month")
}

data class DashboardSummary(
    val totalCft: Double = 0.0,
    val totalBrass: Double = 0.0,
    val totalCubicMeters: Double = 0.0,
    val totalMaterialCost: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grandTotal: Double = 0.0,
    val deliveryCount: Int = 0
)

class ConstructionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ConstructionRepository
    private val aiService = ConstructionAiService()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ConstructionRepository(db.constructionDao())
    }

    val materials: StateFlow<List<MaterialEntity>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawDeliveries: StateFlow<List<TruckDeliveryEntity>> = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    private val _selectedMaterialFilter = MutableStateFlow<String?>(null)
    val selectedMaterialFilter: StateFlow<String?> = _selectedMaterialFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredDeliveries: StateFlow<List<TruckDeliveryEntity>> = combine(
        rawDeliveries,
        _selectedDateFilter,
        _selectedMaterialFilter,
        _searchQuery
    ) { list, dateFilter, matFilter, query ->
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sevenDaysAgo = startOfToday - (6 * 86_400_000L)

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        list.filter { item ->
            val matchDate = when (dateFilter) {
                DateFilter.ALL -> true
                DateFilter.TODAY -> item.dateEpochMs >= startOfToday
                DateFilter.LAST_7_DAYS -> item.dateEpochMs >= sevenDaysAgo
                DateFilter.THIS_MONTH -> item.dateEpochMs >= startOfMonth
            }
            val matchMat = matFilter == null || item.materialName.equals(matFilter, ignoreCase = true)
            val matchQuery = query.isBlank() ||
                item.truckNumber.contains(query, ignoreCase = true) ||
                item.challanNumber.contains(query, ignoreCase = true) ||
                item.driverName.contains(query, ignoreCase = true) ||
                item.supplierName.contains(query, ignoreCase = true)

            matchDate && matchMat && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<DashboardSummary> = filteredDeliveries.combine(MutableStateFlow(Unit)) { list, _ ->
        DashboardSummary(
            totalCft = list.sumOf { it.totalCft },
            totalBrass = list.sumOf { it.totalBrass },
            totalCubicMeters = list.sumOf { it.totalCuM },
            totalMaterialCost = list.sumOf { it.materialCost },
            totalExpenses = list.sumOf { it.totalExpenses },
            grandTotal = list.sumOf { it.grandTotal },
            deliveryCount = list.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    // --- Interactive CFT Calculator State ---
    val calcLengthFt = MutableStateFlow("16")
    val calcLengthIn = MutableStateFlow("0")
    val calcWidthFt = MutableStateFlow("7")
    val calcWidthIn = MutableStateFlow("6")
    val calcHeightFt = MutableStateFlow("4")
    val calcHeightIn = MutableStateFlow("0")
    val calcSelectedMaterial = MutableStateFlow<MaterialEntity?>(null)
    val calcCustomRate = MutableStateFlow("45.0")

    // Weight to CFT State
    val calcGrossTons = MutableStateFlow("30.0")
    val calcTareTons = MutableStateFlow("12.0")

    // PDF Export State
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf.asStateFlow()

    private val _lastGeneratedPdf = MutableStateFlow<File?>(null)
    val lastGeneratedPdf: StateFlow<File?> = _lastGeneratedPdf.asStateFlow()

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    fun setMaterialFilter(material: String?) {
        _selectedMaterialFilter.value = material
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveDelivery(delivery: TruckDeliveryEntity) {
        viewModelScope.launch {
            if (delivery.id == 0L) {
                repository.insertDelivery(delivery)
            } else {
                repository.updateDelivery(delivery)
            }
        }
    }

    fun deleteDelivery(id: Long) {
        viewModelScope.launch {
            repository.deleteDeliveryById(id)
        }
    }

    fun saveMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            if (material.id == 0L) {
                repository.insertMaterial(material)
            } else {
                repository.updateMaterial(material)
            }
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
        }
    }

    fun generatePdfReport(
        projectName: String,
        siteLocation: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val list = filteredDeliveries.value
                val subtitle = "Filter: ${_selectedDateFilter.value.label}" +
                    if (_selectedMaterialFilter.value != null) " | Material: ${_selectedMaterialFilter.value}" else ""
                val file = PdfReportGenerator.generateDeliveryReport(
                    context = getApplication(),
                    projectName = projectName.ifBlank { "Construction Project" },
                    siteLocation = siteLocation.ifBlank { "Main Site Yard" },
                    reportSubtitle = subtitle,
                    deliveries = list
                )
                _lastGeneratedPdf.value = file
                onSuccess(file)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to generate PDF")
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    // --- Gemini AI Features ---
    private val _aiEstimatorState = MutableStateFlow<AiResultState<String>>(AiResultState.Idle)
    val aiEstimatorState: StateFlow<AiResultState<String>> = _aiEstimatorState.asStateFlow()

    private val _aiParserState = MutableStateFlow<AiResultState<ParsedDeliverySlip>>(AiResultState.Idle)
    val aiParserState: StateFlow<AiResultState<ParsedDeliverySlip>> = _aiParserState.asStateFlow()

    private val _aiAuditState = MutableStateFlow<AiResultState<String>>(AiResultState.Idle)
    val aiAuditState: StateFlow<AiResultState<String>> = _aiAuditState.asStateFlow()

    fun estimateProjectMaterials(promptText: String) {
        if (promptText.isBlank()) return
        viewModelScope.launch {
            _aiEstimatorState.value = AiResultState.Loading
            try {
                aiService.estimateMaterialRequirements(promptText).collect { partialText ->
                    _aiEstimatorState.value = AiResultState.Streaming(partialText)
                }
                val current = _aiEstimatorState.value
                if (current is AiResultState.Streaming) {
                    _aiEstimatorState.value = AiResultState.Success(current.partialText)
                }
            } catch (e: TimeoutCancellationException) {
                _aiEstimatorState.value = AiResultState.Error("Gemini estimation timed out after 60s. Please try a shorter prompt.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiEstimatorState.value = AiResultState.Error(e.localizedMessage ?: "AI Material estimation failed.")
            }
        }
    }

    fun parseChallanText(rawText: String) {
        if (rawText.isBlank()) return
        viewModelScope.launch {
            _aiParserState.value = AiResultState.Loading
            try {
                val slip = aiService.parseChallanText(rawText)
                _aiParserState.value = AiResultState.Success(slip)
            } catch (e: TimeoutCancellationException) {
                _aiParserState.value = AiResultState.Error("Parsing timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiParserState.value = AiResultState.Error(e.localizedMessage ?: "Failed to parse delivery slip.")
            }
        }
    }

    fun resetAiParserState() {
        _aiParserState.value = AiResultState.Idle
    }

    fun generateSiteAudit() {
        viewModelScope.launch {
            _aiAuditState.value = AiResultState.Loading
            try {
                val list = rawDeliveries.value
                val auditReport = aiService.auditSiteLogistics(list)
                _aiAuditState.value = AiResultState.Success(auditReport)
            } catch (e: TimeoutCancellationException) {
                _aiAuditState.value = AiResultState.Error("Audit generation timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _aiAuditState.value = AiResultState.Error(e.localizedMessage ?: "Failed to generate AI site audit.")
            }
        }
    }
}
