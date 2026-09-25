package com.core2studio.mymanager.ui.screens.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.firestore.FirestoreClientRepository
import com.core2studio.mymanager.data.firestore.FirestoreOrderRepository
import com.core2studio.mymanager.data.local.dao.DraftOrderDao
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.DraftOrder
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.OrderItemDraft
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.InvoiceGenerator
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import com.core2studio.mymanager.data.repository.SettingsRepository
import com.core2studio.mymanager.utils.DraftAutoSaveManager
import com.core2studio.mymanager.utils.TaxCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

data class CreateOrderUiState(
    val clientId: String = "",
    val clientName: String = "",
    val items: List<OrderItemDraft> = emptyList(),
    val discountType: String = "AMOUNT",
    val discountValue: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING",
    val paymentMethod: String = "Cash",
    val referenceNumber: String = "",
    val notes: String = "",
    val customFields: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val draftId: String? = null,
    val isDraftDirty: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val hasGstin: Boolean = false,
    val gstPricingMode: String = "INCLUSIVE",
    val gstRate: Int = 18,
    val gstType: String = "CGST_SGST"
) {
    private val round2 = { value: Double -> kotlin.math.round(value * 100.0) / 100.0 }

    /** Gross value of every line before discount/tax — what the summary shows as "Subtotal". */
    val subtotal: Double
        get() = items.sumOf { it.subtotal }

    /**
     * Per-line tax breakdown. Computed at most once per state instance instead of on
     * every property read — `orderTotals` and the summary card both read it several
     * times per recomposition.
     */
    val itemBreakdowns: List<TaxCalculator.LineTaxBreakdown> by lazy {
        val pricingMode = TaxCalculator.pricingModeOf(gstPricingMode)
        val gstTypeEnum = TaxCalculator.gstTypeOf(gstType)
        items.map { item ->
            TaxCalculator.calculateLine(
                unitPrice = item.unitPrice,
                quantity = item.quantity,
                pricingMode = pricingMode,
                gstRate = gstRate,
                gstType = gstTypeEnum,
                hasGstin = hasGstin
            )
        }
    }

    /** Order totals. Computed at most once per state instance; single source of truth. */
    val orderTotals: TaxCalculator.OrderTotals? by lazy {
        if (items.isEmpty()) null else TaxCalculator.calculateOrder(
            items = items.zip(itemBreakdowns),
            discountType = discountType,
            discountValue = discountValue,
            pricingMode = TaxCalculator.pricingModeOf(gstPricingMode)
        )
    }

    val discountAmount: Double
        get() = orderTotals?.discountAmount ?: 0.0

    val taxableAmount: Double
        get() = orderTotals?.taxableAmount ?: round2(subtotal)

    val grandTotal: Double
        get() = orderTotals?.grandTotal ?: round2(subtotal)

    val roundedGrandTotal: Long
        get() = orderTotals?.roundedGrandTotal ?: Math.round(grandTotal)

    val roundOffAmount: Double
        get() = orderTotals?.roundOffAmount ?: (roundedGrandTotal - grandTotal)

    val totalTax: Double
        get() = orderTotals?.totalTax ?: 0.0

    val totalCgst: Double
        get() = orderTotals?.totalCgst ?: 0.0

    val totalSgst: Double
        get() = orderTotals?.totalSgst ?: 0.0

    val totalIgst: Double
        get() = orderTotals?.totalIgst ?: 0.0

    val isValid: Boolean
        get() = clientId.isNotBlank() && items.isNotEmpty() && grandTotal > 0
}

class CreateOrderViewModel(
    private val orderRepository: OrderRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val clientRepository: ClientRepository,
    private val firestoreClientRepository: com.core2studio.mymanager.data.firestore.FirestoreClientRepository,
    private val productRepository: ProductRepository,
    private val draftOrderDao: DraftOrderDao,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val applicationScope: kotlinx.coroutines.CoroutineScope
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState

    private var currentSettings = SettingsRepository.BusinessSettings()

    /** Set once the draft is submitted/discarded so a queued auto-save can't re-insert it. */
    @Volatile
    private var draftClosed = false

    private val draftAutoSave = DraftAutoSaveManager<CreateOrderUiState>(
        saveAction = { state -> saveDraftInternal(state) },
        scope = applicationScope
    )

    init {
        // Load settings snapshot for offline draft integrity
        viewModelScope.launch {
            currentSettings = settingsRepository.getMergedSettings()
            _uiState.value = _uiState.value.copy(
                hasGstin = currentSettings.gstEnabled && currentSettings.gstin.isNotBlank(),
                gstPricingMode = currentSettings.gstPricingMode,
                gstRate = currentSettings.gstRate,
                gstType = currentSettings.gstType
            )
        }

        // Observe settings changes
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                currentSettings = settings
                _uiState.value = _uiState.value.copy(
                    hasGstin = settings.gstEnabled && settings.gstin.isNotBlank(),
                    gstPricingMode = settings.gstPricingMode,
                    gstRate = settings.gstRate,
                    gstType = settings.gstType
                )
            }
        }

        // Setup auto-save for drafts - use a simpler approach
        @OptIn(kotlinx.coroutines.FlowPreview::class)
        viewModelScope.launch {
            _uiState
                .debounce(2000L)
                .distinctUntilChanged()
                .filter { it.isDraftDirty }
                .collect { state ->
                    draftAutoSave.scheduleSave(state)
                }
        }
    }

    override fun onCleared() {
        // Persist anything still inside the auto-save window instead of losing it,
        // then stop the application-scoped collector.
        draftAutoSave.flushAndCancel(_uiState.value)
        super.onCleared()
    }

    fun setClient(client: Client) {
        _uiState.value = _uiState.value.copy(
            clientId = client.id,
            clientName = client.name,
            isDraftDirty = true
        )
    }

    fun clearClient() {
        _uiState.value = _uiState.value.copy(
            clientId = "",
            clientName = "",
            isDraftDirty = true
        )
    }

    fun addItem(product: Product?) {
        val newItem = if (product != null) {
            OrderItemDraft(
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                hsnSacCode = product.hsnSacCode,
                hsnSacType = product.hsnSacType
            )
        } else {
            OrderItemDraft(
                productName = "",
                unitPrice = 0.0
            )
        }
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items + newItem,
            isDraftDirty = true
        )
    }

    fun updateItem(index: Int, item: OrderItemDraft) {
        if (index in _uiState.value.items.indices) {
            val newItems = _uiState.value.items.toMutableList()
            newItems[index] = item
            _uiState.value = _uiState.value.copy(
                items = newItems,
                isDraftDirty = true
            )
        }
    }

    fun removeItem(index: Int) {
        val newItems = _uiState.value.items.toMutableList()
        newItems.removeAt(index)
        _uiState.value = _uiState.value.copy(
            items = newItems,
            isDraftDirty = true
        )
    }

    fun setQuantity(index: Int, quantity: Int) {
        if (index in _uiState.value.items.indices) {
            val item = _uiState.value.items[index]
            updateItem(index, item.copy(quantity = maxOf(1, quantity)))
        }
    }

    fun setUnitPrice(index: Int, price: Double) {
        if (index in _uiState.value.items.indices) {
            val item = _uiState.value.items[index]
            updateItem(index, item.copy(unitPrice = price))
        }
    }

    fun setProduct(index: Int, product: Product) {
        if (index in _uiState.value.items.indices) {
            val item = _uiState.value.items[index]
            updateItem(index, item.copy(
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                hsnSacCode = product.hsnSacCode,
                hsnSacType = product.hsnSacType
            ))
        }
    }

    fun setDiscount(type: String, value: Double) {
        _uiState.value = _uiState.value.copy(
            discountType = type,
            discountValue = value,
            isDraftDirty = true
        )
    }

    fun setPaidAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(
            paidAmount = amount,
            isDraftDirty = true
        )
    }

    fun setStatus(status: String) {
        val newPaidAmount = when (status) {
            "PENDING" -> 0.0
            "COMPLETED" -> _uiState.value.grandTotal
            else -> _uiState.value.paidAmount
        }
        _uiState.value = _uiState.value.copy(
            status = status,
            paidAmount = newPaidAmount,
            isDraftDirty = true
        )
    }

    fun setPaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(
            paymentMethod = method,
            isDraftDirty = true
        )
    }

    fun setReferenceNumber(ref: String) {
        _uiState.value = _uiState.value.copy(
            referenceNumber = ref,
            isDraftDirty = true
        )
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(
            notes = notes,
            isDraftDirty = true
        )
    }

    fun setCustomFields(fields: Map<String, String>) {
        _uiState.value = _uiState.value.copy(
            customFields = fields,
            isDraftDirty = true
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun saveDraft() {
        val state = _uiState.value
        if (state.clientId.isBlank() && state.items.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Add client or items before saving draft")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            saveDraftInternal(state)
            _uiState.value = _uiState.value.copy(isLoading = false, successMessage = "Draft saved successfully")
        }
    }

    private fun saveDraftInternal(state: CreateOrderUiState) {
        if (draftClosed) return
        // Never persist an empty draft (also guards the post-submit/post-discard flush).
        if (state.clientId.isBlank() && state.items.isEmpty()) return
        // applicationScope (not viewModelScope): this is also invoked from onCleared(),
        // at which point viewModelScope is already cancelled.
        applicationScope.launch {
            val uid = authRepository.userId ?: return@launch
            val draftId = state.draftId ?: UUID.randomUUID().toString()
            try {
                val itemsJson = json.encodeToString(state.items)
                val customFieldsJson = json.encodeToString(state.customFields)

                val draft = DraftOrder(
                    id = draftId,
                    userId = uid,
                    clientId = state.clientId,
                    clientName = state.clientName,
                    itemsJson = itemsJson,
                    discountType = state.discountType,
                    discountValue = state.discountValue,
                    gstPricingMode = state.gstPricingMode,
                    gstRate = state.gstRate,
                    gstType = state.gstType,
                    hasGstin = state.hasGstin,
                    paidAmount = state.paidAmount,
                    status = state.status,
                    paymentMethod = state.paymentMethod,
                    referenceNumber = state.referenceNumber,
                    notes = state.notes,
                    customFieldsJson = customFieldsJson,
                    updatedAt = System.currentTimeMillis(),
                    createdAt = state.createdAt
                )

                draftOrderDao.insert(draft)
                _uiState.value = _uiState.value.copy(draftId = draftId, isDraftDirty = false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save draft", e)
            }
        }
    }

    fun loadDraft(draftId: String) {
        viewModelScope.launch {
            draftClosed = false
            val draft = draftOrderDao.getDraftById(draftId)
            draft?.let {
                val items = try {
                    json.decodeFromString<List<OrderItemDraft>>(it.itemsJson)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse draft items", e)
                    emptyList()
                }
                val customFields = try {
                    json.decodeFromString<Map<String, String>>(it.customFieldsJson)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse draft custom fields", e)
                    emptyMap()
                }
                _uiState.value = CreateOrderUiState(
                    clientId = it.clientId,
                    clientName = it.clientName,
                    items = items,
                    discountType = it.discountType,
                    discountValue = it.discountValue,
                    paidAmount = it.paidAmount,
                    status = it.status,
                    paymentMethod = it.paymentMethod,
                    referenceNumber = it.referenceNumber,
                    notes = it.notes,
                    customFields = customFields,
                    draftId = it.id,
                    hasGstin = it.hasGstin,
                    gstPricingMode = it.gstPricingMode,
                    gstRate = it.gstRate,
                    gstType = it.gstType,
                    createdAt = it.createdAt
                )
            }
        }
    }

    fun submitOrder() {
        if (!_uiState.value.isValid) return
        val state = _uiState.value

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val uid = authRepository.userId
            if (uid == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Your session has expired. Please sign in again."
                )
                return@launch
            }

            // Build cart items JSON for multi-item orders
            val cartItemsJson = state.items.map { item ->
                mapOf(
                    "productName" to item.productName,
                    "price" to item.unitPrice.toString(),
                    "quantity" to item.quantity.toString(),
                    "subtotal" to item.subtotal.toString(),
                    "hsnSacCode" to item.hsnSacCode,
                    "hsnSacType" to item.hsnSacType
                )
            }
            val allCustomFields = state.customFields.toMutableMap()
            allCustomFields["cartItems"] = json.encodeToString(cartItemsJson)
            if (state.discountValue > 0) {
                allCustomFields["discountType"] = state.discountType
                allCustomFields["discountValue"] = state.discountValue.toString()
            }
            if (state.referenceNumber.isNotBlank()) {
                allCustomFields["referenceNumber"] = state.referenceNumber
            }
            if (state.hasGstin) {
                allCustomFields["gstRate"] = state.gstRate.toString()
                allCustomFields["gstType"] = state.gstType
                allCustomFields["gstPricingMode"] = state.gstPricingMode
            }

            val order = Order(
                clientId = state.clientId,
                productName = if (state.items.size == 1) state.items.first().productName else state.items.joinToString(", ") { it.productName },
                quantity = state.items.sumOf { it.quantity },
                unitPrice = if (state.items.size == 1) state.items.first().unitPrice else 0.0,
                amount = state.roundedGrandTotal.toDouble(),
                paidAmount = state.paidAmount,
                status = state.status,
                paymentMethod = state.paymentMethod,
                customFields = json.encodeToString(allCustomFields),
                notes = state.notes
            )

            firestoreOrderRepository.insertOrder(uid, order).onSuccess {
                // Stop the auto-save queue first: a debounced save still in flight
                // would otherwise re-insert the draft we are about to delete.
                draftClosed = true
                draftAutoSave.clearPending()
                state.draftId?.let { draftOrderDao.deleteDraft(it) }
                _uiState.value = CreateOrderUiState(
                    successMessage = "Order saved successfully"
                )
            }.onFailure { e ->
                Log.e(TAG, "Failed to create order", e)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Failed to save order: ${e.message}")
            }
        }
    }

    fun discardDraft() {
        viewModelScope.launch {
            draftClosed = true
            draftAutoSave.clearPending()
            val state = _uiState.value
            state.draftId?.let { draftOrderDao.deleteDraft(it) }
            _uiState.value = CreateOrderUiState()
        }
    }

    fun getAuthRepository(): AuthRepository = authRepository
    fun getClientRepository(): ClientRepository = clientRepository
    fun addClientFromScreen(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val uid = authRepository.userId
            if (uid == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "You must be signed in to add a client")
                return@launch
            }
            val client = Client(
                id = "",
                name = name,
                phone = phone,
                email = email,
                address = address,
                createdAt = System.currentTimeMillis()
            )
            // Write through Firestore so the client gets a real generated id and is
            // persisted remotely — a local-only insert with id="" would overwrite the
            // previous row and crash on navigate("client/").
            firestoreClientRepository.insertClient(uid, client)
                .onSuccess { newId ->
                    setClient(client.copy(id = newId))
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to add client from create-order screen", e)
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to save client: ${e.message}")
                }
        }
    }

    companion object {
        private const val TAG = "CreateOrderViewModel"

        /**
         * Lenient JSON: a draft written by another app version (or a hand-edited row)
         * must degrade gracefully instead of crashing the coroutine with
         * SerializationException.
         */
        private val json = Json { ignoreUnknownKeys = true }
    }
}