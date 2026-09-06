package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AiTradingAdvice
import com.example.data.model.Asset
import com.example.data.model.AssetCategory
import com.example.data.model.CandleStick
import com.example.data.model.EconomicEvent
import com.example.data.model.IndicatorType
import com.example.data.model.MarketMoverAnalysis
import com.example.data.model.MarketNews
import com.example.data.model.NotificationItem
import com.example.data.model.OrderSide
import com.example.data.model.PaymentMethod
import com.example.data.model.Position
import com.example.data.model.TradingSignal
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.BrokerRepository
import com.example.data.service.PayPalWebhookEvent
import com.example.data.service.WebhookListenerStatus
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.ThemePreset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BrokerTab {
    DASHBOARD,
    TRADE,
    POSITIONS,
    BANKING,
    MARKETS,
    ANALYTICS,
    ADMIN,
    SUPPORT,
    DOCS,
    PROFILE
}

data class ChatMessage(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromUser: Boolean = false
)

class BrokerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = BrokerRepository(database)

    // UI Configuration State
    private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _themePreset = MutableStateFlow(ThemePreset.APEX_CYAN_PRO)
    val themePreset: StateFlow<ThemePreset> = _themePreset.asStateFlow()

    private val _activeTab = MutableStateFlow(BrokerTab.DASHBOARD)
    val activeTab: StateFlow<BrokerTab> = _activeTab.asStateFlow()

    // Core Data Streams from Repository
    val assets: StateFlow<List<Asset>> = repository.assets
    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val tradingSignals: StateFlow<List<TradingSignal>> = repository.tradingSignals
    val marketNews: StateFlow<List<MarketNews>> = repository.marketNews
    val economicEvents: StateFlow<List<EconomicEvent>> = repository.economicEvents
    val marketMovers: StateFlow<List<MarketMoverAnalysis>> = repository.marketMovers
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val isLiveInternetDirect: StateFlow<Boolean> = repository.isLiveInternetDirect
    val liveMarketStatus = repository.liveMarketStatus
    val mt5LatencyMs: StateFlow<Int> = repository.mt5LatencyMs
    val mt5Connected: StateFlow<Boolean> = repository.mt5Connected

    // PayPal Sandbox Real-Time Webhook Listener State
    val webhookListenerStatus: StateFlow<WebhookListenerStatus> = repository.webhookListenerStatus
    val webhookEvents: StateFlow<List<PayPalWebhookEvent>> = repository.webhookEvents
    val trackedWithdrawalIds: StateFlow<Set<String>> = repository.trackedWithdrawalIds
    private val _selectedWebhookEvent = MutableStateFlow<PayPalWebhookEvent?>(null)
    val selectedWebhookEvent: StateFlow<PayPalWebhookEvent?> = _selectedWebhookEvent.asStateFlow()

    // AI Trading Agent Recommendation State
    private val _aiAdvice = MutableStateFlow<AiTradingAdvice?>(null)
    val aiAdvice: StateFlow<AiTradingAdvice?> = _aiAdvice.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    val openPositions: StateFlow<List<Position>> = repository.openPositions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val closedPositions: StateFlow<List<Position>> = repository.closedPositions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions: StateFlow<List<Transaction>> = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Trading Chart State
    private val _selectedAsset = MutableStateFlow<Asset?>(null)
    val selectedAsset: StateFlow<Asset?> = _selectedAsset.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1H")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _selectedIndicator = MutableStateFlow(IndicatorType.SMA)
    val selectedIndicator: StateFlow<IndicatorType> = _selectedIndicator.asStateFlow()

    private val _isCandleMode = MutableStateFlow(true)
    val isCandleMode: StateFlow<Boolean> = _isCandleMode.asStateFlow()

    private val _candlesticks = MutableStateFlow<List<CandleStick>>(emptyList())
    val candlesticks: StateFlow<List<CandleStick>> = _candlesticks.asStateFlow()

    // Dialogs and Modals
    private val _selectedReceipt = MutableStateFlow<Transaction?>(null)
    val selectedReceipt: StateFlow<Transaction?> = _selectedReceipt.asStateFlow()

    private val _isOrderSheetOpen = MutableStateFlow(false)
    val isOrderSheetOpen: StateFlow<Boolean> = _isOrderSheetOpen.asStateFlow()

    private val _orderSheetSide = MutableStateFlow(OrderSide.BUY)
    val orderSheetSide: StateFlow<OrderSide> = _orderSheetSide.asStateFlow()

    // Screener State
    private val _screenerCategory = MutableStateFlow(AssetCategory.ALL)
    val screenerCategory: StateFlow<AssetCategory> = _screenerCategory.asStateFlow()

    private val _screenerQuery = MutableStateFlow("")
    val screenerQuery: StateFlow<String> = _screenerQuery.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    // Customer Support Live Chat
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "C1",
                sender = "Apex Broker AI Specialist",
                message = "Welcome to Apex VIP Support. How can we assist with your MT5 orders, PayPal deposits, or leverage inquiries today?",
                isFromUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatTyping = MutableStateFlow(false)
    val isChatTyping: StateFlow<Boolean> = _isChatTyping.asStateFlow()

    private var chartLoadJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            // Keep selected asset and active live candlestick in sync with real-time asset price ticks
            repository.assets.collect { list ->
                if (_selectedAsset.value == null && list.isNotEmpty()) {
                    selectAsset(list.first())
                } else if (_selectedAsset.value != null) {
                    val currentSym = _selectedAsset.value!!.symbol
                    val updated = list.find { it.symbol == currentSym }
                    if (updated != null) {
                        _selectedAsset.value = updated
                        updateActiveLiveCandle(updated.price)
                    }
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
    }

    fun toggleLanguage() {
        _appLanguage.value = if (_appLanguage.value == AppLanguage.ENGLISH) AppLanguage.ARABIC else AppLanguage.ENGLISH
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setThemePreset(preset: ThemePreset) {
        _themePreset.value = preset
        if (preset == ThemePreset.CLEAN_LIGHT) {
            _isDarkMode.value = false
        }
    }

    fun selectTab(tab: BrokerTab) {
        _activeTab.value = tab
    }

    fun selectAsset(asset: Asset) {
        _selectedAsset.value = asset
        loadChartData(asset.symbol, _selectedTimeframe.value)
        requestAiTradingAdvice(asset.symbol, _selectedTimeframe.value)
    }

    fun setTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        _selectedAsset.value?.let { 
            loadChartData(it.symbol, tf)
            requestAiTradingAdvice(it.symbol, tf)
        }
    }

    fun requestAiTradingAdvice(symbol: String? = null, timeframe: String = _selectedTimeframe.value) {
        val targetSymbol = symbol ?: _selectedAsset.value?.symbol ?: "NVDA"
        val isAr = _appLanguage.value == AppLanguage.ARABIC
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val advice = repository.getAiTradingAdvice(targetSymbol, timeframe, isAr)
                _aiAdvice.value = advice
            } catch (e: Exception) {
                // Keep existing advice or fallback
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun setIndicator(indicator: IndicatorType) {
        _selectedIndicator.value = indicator
    }

    fun toggleChartStyle() {
        _isCandleMode.value = !_isCandleMode.value
    }

    private fun loadChartData(symbol: String, timeframe: String) {
        // 1. Instant high-fidelity sync load
        _candlesticks.value = repository.getCandlesticksForAssetSync(symbol, timeframe)

        // 2. Asynchronous fetch of real live market candles from official endpoints
        chartLoadJob?.cancel()
        chartLoadJob = viewModelScope.launch {
            try {
                val realCandles = repository.getCandlesticksForAsset(symbol, timeframe)
                if (realCandles.isNotEmpty()) {
                    _candlesticks.value = realCandles
                }
            } catch (e: Exception) {
                // Keep instant sync candles on exception
            }
        }
    }

    private fun updateActiveLiveCandle(livePrice: Double) {
        val currentList = _candlesticks.value
        if (currentList.isNotEmpty() && livePrice > 0.0) {
            val last = currentList.last()
            val now = System.currentTimeMillis()
            val intervalMs = when (_selectedTimeframe.value) {
                "1M" -> 60000L
                "5M" -> 300000L
                "15M" -> 900000L
                "30M" -> 1800000L
                "1H" -> 3600000L
                "4H" -> 14400000L
                "1D" -> 86400000L
                "1W" -> 604800000L
                else -> 60000L
            }

            if (now - last.timestamp >= intervalMs) {
                // Time interval rolled over - push new candle to historical sequence
                val newCandle = CandleStick(
                    timestamp = now,
                    open = last.close,
                    high = maxOf(last.close, livePrice.toFloat()),
                    low = minOf(last.close, livePrice.toFloat()),
                    close = livePrice.toFloat(),
                    volume = (livePrice * 25.0).toFloat()
                )
                _candlesticks.value = (currentList.takeLast(75) + newCandle)
            } else {
                val newClose = livePrice.toFloat()
                val newHigh = maxOf(last.high, newClose)
                val newLow = minOf(last.low, newClose)
                if (last.close != newClose || last.high != newHigh || last.low != newLow) {
                    val updatedLast = last.copy(
                        close = newClose,
                        high = newHigh,
                        low = newLow,
                        volume = last.volume + (livePrice.toFloat() * 0.15f)
                    )
                    _candlesticks.value = currentList.dropLast(1) + updatedLast
                }
            }
        }
    }

    fun openOrderSheet(asset: Asset, side: OrderSide) {
        _selectedAsset.value = asset
        _orderSheetSide.value = side
        _isOrderSheetOpen.value = true
    }

    fun closeOrderSheet() {
        _isOrderSheetOpen.value = false
    }

    fun showReceipt(transaction: Transaction) {
        _selectedReceipt.value = transaction
    }

    fun dismissReceipt() {
        _selectedReceipt.value = null
    }

    fun executeOrder(
        symbol: String,
        side: OrderSide,
        lots: Double,
        leverage: Int,
        stopLoss: Double?,
        takeProfit: Double?
    ) {
        viewModelScope.launch {
            repository.executeOrder(symbol, side, lots, leverage, stopLoss, takeProfit)
        }
    }

    fun closePosition(positionId: String) {
        viewModelScope.launch {
            repository.closePosition(positionId)
        }
    }

    fun depositPayPal(amount: Double, email: String) {
        viewModelScope.launch {
            val tx = repository.processPayPalDeposit(amount, email)
            _selectedReceipt.value = tx
        }
    }

    fun requestWithdrawal(amount: Double, method: PaymentMethod, destination: String, notes: String = "") {
        viewModelScope.launch {
            val result = repository.processWithdrawal(amount, method, destination, notes)
            result.onSuccess { tx ->
                _selectedReceipt.value = tx
            }
        }
    }

    fun selectWebhookEvent(event: PayPalWebhookEvent?) {
        _selectedWebhookEvent.value = event
    }

    fun forceWebhookVerification(transactionId: String, destination: String, amount: Double) {
        repository.forceWebhookVerification(transactionId, destination, amount)
    }

    fun toggleFavorite(symbol: String, isCurrentlyFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(symbol, isCurrentlyFavorite)
        }
    }

    fun setOfflineMode(offline: Boolean) {
        repository.setOfflineMode(offline)
    }

    fun toggleMt5Gateway() {
        repository.toggleMt5Connection()
    }

    fun switchUserRole(role: UserRole) {
        repository.switchUserRole(role)
    }

    fun updateSecuritySettings(twoFactor: Boolean, biometric: Boolean) {
        repository.updateSecuritySettings(twoFactor, biometric)
    }

    fun setScreenerCategory(category: AssetCategory) {
        _screenerCategory.value = category
    }

    fun setScreenerQuery(query: String) {
        _screenerQuery.value = query
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun refreshLivePrices() {
        repository.refreshLivePrices()
    }

    fun toggleLiveDirectInternet(enable: Boolean) {
        repository.toggleLiveDirectInternet(enable)
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun sendSupportMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(
            id = "U-" + System.currentTimeMillis(),
            sender = userProfile.value.fullName,
            message = userText,
            isFromUser = true
        )
        _chatMessages.update { it + userMsg }
        _isChatTyping.value = true

        viewModelScope.launch {
            delay(1200)
            val replyText = generateBotReply(userText)
            val botMsg = ChatMessage(
                id = "B-" + System.currentTimeMillis(),
                sender = "Apex Broker Specialist (Desk #4)",
                message = replyText,
                isFromUser = false
            )
            _chatMessages.update { it + botMsg }
            _isChatTyping.value = false
        }
    }

    private fun generateBotReply(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("paypal") || lower.contains("deposit") -> {
                "PayPal deposits are processed instantly via our v2 Rest API integration with 0% processing fee promotion. Funds reflect immediately in your MT5 free margin."
            }
            lower.contains("withdraw") -> {
                "Withdrawals are processed under Tier-2 AML automated checks. PayPal withdrawals take under 10 minutes, while SWIFT wire transfers take 1-2 banking business days."
            }
            lower.contains("mt5") || lower.contains("metatrader") || lower.contains("server") -> {
                "Your account is connected to server 'ApexBroker-Live-01' via low-latency FIX protocol (18ms). You can trade Forex, Equities, Crypto, and Commodities directly with up to 1:100 leverage."
            }
            lower.contains("leverage") || lower.contains("margin") -> {
                "Maximum leverage is 1:100 for Forex majors and 1:20 for single-stock CFDs. Margin call warning triggers at 100% margin level, and automated stop-out occurs at 50%."
            }
            lower.contains("arabic") || lower.contains("عربي") -> {
                "نحن نوفر دعماً كاملاً باللغة العربية مع واجهة RTL متوافقة. يمكنك تبديل اللغة من القائمة العلوية أو من صفحة الإعدادات في أي وقت."
            }
            else -> {
                "Thank you for contacting Apex VIP Desk. We have logged your query regarding '${prompt.take(30)}...'. A senior compliance & liquidity desk manager has verified your active MT5 session."
            }
        }
    }
}
