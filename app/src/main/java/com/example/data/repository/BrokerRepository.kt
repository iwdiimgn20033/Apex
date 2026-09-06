package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PortfolioEntity
import com.example.data.local.entities.PositionEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.WatchlistEntity
import com.example.data.model.AiTradingAdvice
import com.example.data.model.Asset
import com.example.data.model.AssetCategory
import com.example.data.model.CandleStick
import com.example.data.model.CatalystCategory
import com.example.data.model.EconomicEvent
import com.example.data.model.KycStatus
import com.example.data.model.KycTier
import com.example.data.model.LiveMarketStatus
import com.example.data.model.MarketMoverAnalysis
import com.example.data.model.MarketNews
import com.example.data.model.MarketSentiment
import com.example.data.model.NewsImpact
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.OrderSide
import com.example.data.model.PaymentMethod
import com.example.data.model.Position
import com.example.data.model.PositionStatus
import com.example.data.model.SignalAction
import com.example.data.model.TradingSignal
import com.example.data.model.Transaction
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.service.GeminiTradingAgent
import com.example.data.service.LiveMarketDataService
import com.example.data.service.LivePriceUpdate
import com.example.data.service.PayPalWebhookEvent
import com.example.data.service.PayPalWebhookSimulationService
import com.example.data.service.WebhookListenerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sin
import kotlin.random.Random

class BrokerRepository(private val database: AppDatabase) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // In-memory state for real-time reactivity
    private val _assets = MutableStateFlow<List<Asset>>(getInitialAssets())
    val assets: StateFlow<List<Asset>> = _assets.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            userId = "APEX-TRADER-9821",
            fullName = "Alexander Vance",
            email = "alex.vance@apexbroker.com",
            phoneNumber = "+1 (555) 789-2041",
            kycTier = KycTier.TIER_2,
            kycStatus = KycStatus.VERIFIED,
            role = UserRole.TRADER,
            twoFactorEnabled = true,
            biometricEnabled = true,
            mt5AccountNumber = "MT5-884920",
            mt5Server = "ApexBroker-Live-01.mt5server.net",
            leverageAllowed = 100,
            baseCurrency = "USD",
            totalBalance = 75480.00,
            equity = 78620.50,
            usedMargin = 6840.00,
            freeMargin = 71780.50,
            marginLevelPercent = 1149.4,
            todayPnl = 3140.50,
            todayPnlPercent = 4.15,
            allTimeReturnPercent = 62.4
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _tradingSignals = MutableStateFlow<List<TradingSignal>>(getInitialSignals())
    val tradingSignals: StateFlow<List<TradingSignal>> = _tradingSignals.asStateFlow()

    private val _marketNews = MutableStateFlow<List<MarketNews>>(getInitialNews())
    val marketNews: StateFlow<List<MarketNews>> = _marketNews.asStateFlow()

    private val _economicEvents = MutableStateFlow<List<EconomicEvent>>(getInitialEconomicEvents())
    val economicEvents: StateFlow<List<EconomicEvent>> = _economicEvents.asStateFlow()

    private val _marketMovers = MutableStateFlow<List<MarketMoverAnalysis>>(getInitialMarketMovers())
    val marketMovers: StateFlow<List<MarketMoverAnalysis>> = _marketMovers.asStateFlow()

    private val aiTradingAgent = GeminiTradingAgent()
    private val liveMarketService = LiveMarketDataService()
    val payPalWebhookService = PayPalWebhookSimulationService(database)

    val liveMarketStatus: StateFlow<LiveMarketStatus> = liveMarketService.status
    val webhookListenerStatus: StateFlow<WebhookListenerStatus> = payPalWebhookService.listenerStatus
    val webhookEvents: StateFlow<List<PayPalWebhookEvent>> = payPalWebhookService.recentEvents
    val trackedWithdrawalIds: StateFlow<Set<String>> = payPalWebhookService.trackedTransactionIds

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isLiveInternetDirect = MutableStateFlow(true)
    val isLiveInternetDirect: StateFlow<Boolean> = _isLiveInternetDirect.asStateFlow()

    private val _mt5LatencyMs = MutableStateFlow(18)
    val mt5LatencyMs: StateFlow<Int> = _mt5LatencyMs.asStateFlow()

    private val _mt5Connected = MutableStateFlow(true)
    val mt5Connected: StateFlow<Boolean> = _mt5Connected.asStateFlow()

    // Room DB streams
    val openPositions: Flow<List<Position>> = database.positionDao().getOpenPositions().map { list ->
        list.map { it.toPosition() }
    }

    val closedPositions: Flow<List<Position>> = database.positionDao().getClosedPositions().map { list ->
        list.map { it.toPosition() }
    }

    val transactions: Flow<List<Transaction>> = database.transactionDao().getAllTransactions().map { list ->
        list.map { it.toTransaction() }
    }

    val notifications: Flow<List<NotificationItem>> = database.notificationDao().getNotifications().map { list ->
        list.map { it.toNotificationItem() }
    }

    val favoriteSymbols: Flow<List<String>> = database.watchlistDao().getFavoriteSymbols()

    init {
        coroutineScope.launch {
            seedInitialDataIfNeeded()
            startLiveInternetPriceFeed()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val existingTx = database.transactionDao().getAllTransactions().firstOrNull()
        if (existingTx.isNullOrEmpty()) {
            val initialPositions = listOf(
                Position(
                    id = "POS-001",
                    symbol = "NVDA",
                    assetName = "NVIDIA Corporation",
                    side = OrderSide.BUY,
                    volumeLots = 50.0,
                    openPrice = 205.00,
                    currentPrice = 217.50,
                    stopLoss = 195.00,
                    takeProfit = 240.00,
                    floatingPnL = 6250.00,
                    marginRequired = 2050.00,
                    leverage = 20,
                    openTimestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                Position(
                    id = "POS-002",
                    symbol = "EURUSD",
                    assetName = "Euro / US Dollar",
                    side = OrderSide.BUY,
                    volumeLots = 2.0,
                    openPrice = 1.0825,
                    currentPrice = 1.0874,
                    stopLoss = 1.0780,
                    takeProfit = 1.0960,
                    floatingPnL = 980.00,
                    marginRequired = 2165.00,
                    leverage = 100,
                    openTimestamp = System.currentTimeMillis() - 86400000L
                ),
                Position(
                    id = "POS-003",
                    symbol = "BTCUSD",
                    assetName = "Bitcoin / USD",
                    side = OrderSide.SELL,
                    volumeLots = 0.5,
                    openPrice = 79200.00,
                    currentPrice = 77643.00,
                    stopLoss = 81000.00,
                    takeProfit = 74000.00,
                    floatingPnL = 778.50,
                    marginRequired = 3960.00,
                    leverage = 10,
                    openTimestamp = System.currentTimeMillis() - 3600000L * 5
                )
            )
            database.positionDao().insertAll(initialPositions.map { it.toEntity() })

            val initialTransactions = listOf(
                Transaction(
                    id = "TX-99014",
                    type = TransactionType.DEPOSIT,
                    amount = 25000.00,
                    method = PaymentMethod.PAYPAL,
                    status = TransactionStatus.COMPLETED,
                    timestamp = System.currentTimeMillis() - 86400000L * 7,
                    referenceHash = "PP-89A019F4B3C9",
                    accountDestination = "Apex MT5 Account #884920",
                    notes = "Instant PayPal deposit verified via Webhook API"
                ),
                Transaction(
                    id = "TX-99015",
                    type = TransactionType.DEPOSIT,
                    amount = 50000.00,
                    method = PaymentMethod.BANK_TRANSFER,
                    status = TransactionStatus.COMPLETED,
                    timestamp = System.currentTimeMillis() - 86400000L * 14,
                    referenceHash = "SWIFT-CHASE-7729104",
                    accountDestination = "Apex Brokerage Clearing Account",
                    notes = "SWIFT Wire Transfer / Fedwire cleared"
                ),
                Transaction(
                    id = "TX-99016",
                    type = TransactionType.TRADE_PROFIT,
                    amount = 5840.00,
                    method = PaymentMethod.BANK_TRANSFER,
                    status = TransactionStatus.COMPLETED,
                    timestamp = System.currentTimeMillis() - 86400000L * 3,
                    referenceHash = "MT5-CLO-339182",
                    accountDestination = "Realized Trading Profit - AAPL Long",
                    notes = "Closed 100 shares @ $322.80"
                )
            )
            database.transactionDao().insertAll(initialTransactions.map { it.toEntity() })

            val initialNotifs = listOf(
                NotificationItem(
                    id = "NOTIF-1",
                    title = "PayPal Deposit Cleared",
                    titleAr = "تم تأكيد إيداع باي بال",
                    message = "$25,000.00 USD added instantly to MT5 balance.",
                    messageAr = "تمت إضافة 25,000.00 دولار فوراً إلى رصيد MT5.",
                    type = NotificationType.PAYMENT,
                    timestamp = System.currentTimeMillis() - 86400000L * 2
                ),
                NotificationItem(
                    id = "NOTIF-2",
                    title = "MT5 Execution: NVDA Long",
                    titleAr = "تنفيذ MT5: شراء سهم إنفيديا",
                    message = "Executed Buy 50.0 lots NVDA @ $124.50 (Ticket #884102).",
                    messageAr = "تم تنفيذ شراء 50 لوت لسهم NVDA بسعر 124.50$ (تذكرة #884102).",
                    type = NotificationType.TRADE,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                NotificationItem(
                    id = "NOTIF-3",
                    title = "New AI Signal: EUR/USD Breakout",
                    titleAr = "إشارة تداول ذكية: اختراق اليورو/دولار",
                    message = "RSI bullish divergence detected on 1H chart. Target: 1.0960.",
                    messageAr = "تم رصد انفراج إيجابي على مؤشر RSI في إطار الساعة. الهدف: 1.0960.",
                    type = NotificationType.SIGNAL,
                    timestamp = System.currentTimeMillis() - 3600000L * 3
                )
            )
            initialNotifs.forEach { database.notificationDao().insertNotification(it.toEntity()) }
        }
    }

    private fun startLiveInternetPriceFeed() {
        coroutineScope.launch {
            liveMarketService.startLiveFeed(3000L)
            liveMarketService.priceUpdates.collect { liveList ->
                if (!_isOfflineMode.value && _isLiveInternetDirect.value) {
                    val updateMap = liveList.associateBy { it.symbol }
                    _assets.update { currentList ->
                        currentList.map { asset ->
                            val update = updateMap[asset.symbol]
                            if (update != null) {
                                val roundedPrice = update.price
                                val newBid = update.bid
                                val newAsk = update.ask
                                val newHistory = (asset.historicalPoints + roundedPrice).takeLast(20)

                                asset.copy(
                                    price = roundedPrice,
                                    bid = newBid,
                                    ask = newAsk,
                                    change24h = update.change24h,
                                    changePercent24h = update.changePercent24h,
                                    high24h = maxOf(asset.high24h, update.high24h),
                                    low24h = if (asset.low24h > 0) minOf(asset.low24h, update.low24h) else update.low24h,
                                    volume = update.volume ?: asset.volume,
                                    historicalPoints = if (newHistory.isNotEmpty()) newHistory else asset.historicalPoints
                                )
                            } else {
                                asset
                            }
                        }
                    }

                    // Recalculate floating P&L for open positions in real time based on live market quotes
                    updateOpenPositionsFloatingPnL()
                }
            }
        }
    }

    private suspend fun updateOpenPositionsFloatingPnL() {
        val openList = database.positionDao().getOpenPositions().firstOrNull() ?: return
        var totalFloatingPnL = 0.0

        for (pos in openList) {
            val asset = _assets.value.find { it.symbol == pos.symbol }
            if (asset != null) {
                val currentPrice = if (pos.side == OrderSide.BUY.name) asset.bid else asset.ask
                val multiplier = if (pos.side == OrderSide.BUY.name) 1 else -1
                val lotMultiplier = if (asset.category == AssetCategory.FOREX) 100000.0 else 1.0
                val pnl = (currentPrice - pos.openPrice) * pos.volumeLots * lotMultiplier * multiplier

                database.positionDao().updatePosition(
                    pos.copy(
                        currentPrice = currentPrice,
                        floatingPnL = pnl
                    )
                )
                totalFloatingPnL += pnl
            }
        }

        // Dynamically update user profile Equity and Margin Level based on live market
        _userProfile.update { profile ->
            val newEquity = profile.totalBalance + totalFloatingPnL
            val freeMargin = newEquity - profile.usedMargin
            val marginLevel = if (profile.usedMargin > 0) (newEquity / profile.usedMargin) * 100.0 else 0.0
            profile.copy(
                equity = newEquity,
                freeMargin = freeMargin,
                marginLevelPercent = marginLevel,
                todayPnl = totalFloatingPnL
            )
        }
    }

    fun refreshLivePrices() {
        liveMarketService.triggerImmediateSync()
    }

    fun toggleLiveDirectInternet(enable: Boolean) {
        _isLiveInternetDirect.value = enable
        if (enable) {
            liveMarketService.startLiveFeed(3000L)
            liveMarketService.triggerImmediateSync()
        } else {
            liveMarketService.stopLiveFeed()
        }
    }

    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
        if (offline) {
            _mt5Connected.value = false
        } else {
            _mt5Connected.value = true
        }
    }

    fun toggleMt5Connection() {
        if (!_isOfflineMode.value) {
            _mt5Connected.value = !_mt5Connected.value
        }
    }

    fun switchUserRole(role: UserRole) {
        _userProfile.update { it.copy(role = role) }
    }

    fun updateSecuritySettings(twoFactor: Boolean, biometric: Boolean) {
        _userProfile.update { it.copy(twoFactorEnabled = twoFactor, biometricEnabled = biometric) }
    }

    suspend fun toggleFavorite(symbol: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            database.watchlistDao().removeFavorite(symbol)
        } else {
            database.watchlistDao().toggleFavorite(WatchlistEntity(symbol = symbol, isFavorite = true))
        }
        _assets.update { list ->
            list.map { if (it.symbol == symbol) it.copy(isFavorite = !isCurrentlyFavorite) else it }
        }
    }

    suspend fun executeOrder(
        symbol: String,
        side: OrderSide,
        lots: Double,
        leverage: Int,
        stopLoss: Double?,
        takeProfit: Double?
    ): Result<Position> {
        val asset = _assets.value.find { it.symbol == symbol } ?: return Result.failure(Exception("Asset not found"))
        val execPrice = if (side == OrderSide.BUY) asset.ask else asset.bid
        val notionalValue = execPrice * lots * (if (asset.category == AssetCategory.FOREX) 100000 else 1)
        val marginRequired = notionalValue / leverage

        if (marginRequired > _userProfile.value.freeMargin) {
            return Result.failure(Exception("Insufficient free margin. Required: $${String.format(Locale.US, "%.2f", marginRequired)}"))
        }

        val newPosition = Position(
            id = "POS-" + UUID.randomUUID().toString().substring(0, 8).uppercase(),
            symbol = symbol,
            assetName = asset.name,
            side = side,
            volumeLots = lots,
            openPrice = execPrice,
            currentPrice = execPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            floatingPnL = 0.0,
            marginRequired = marginRequired,
            leverage = leverage,
            openTimestamp = System.currentTimeMillis()
        )

        database.positionDao().insertPosition(newPosition.toEntity())

        // Recalculate margins
        _userProfile.update { profile ->
            val updatedUsedMargin = profile.usedMargin + marginRequired
            val updatedFreeMargin = profile.equity - updatedUsedMargin
            profile.copy(
                usedMargin = updatedUsedMargin,
                freeMargin = updatedFreeMargin,
                marginLevelPercent = if (updatedUsedMargin > 0) (profile.equity / updatedUsedMargin) * 100 else 0.0
            )
        }

        // Add notification
        val notif = NotificationEntity(
            id = "NOTIF-" + System.currentTimeMillis(),
            title = "Order Executed (${side.name})",
            titleAr = "تم تنفيذ الصفقة (${if (side == OrderSide.BUY) "شراء" else "بيع"})",
            message = "Executed $lots lots of $symbol @ $execPrice via MT5 Gateway.",
            messageAr = "تم تنفيذ عقد بحجم $lots لوت لـ $symbol بسعر $execPrice عبر بوابة MT5.",
            type = "TRADE",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        database.notificationDao().insertNotification(notif)

        return Result.success(newPosition)
    }

    suspend fun closePosition(positionId: String): Result<Double> {
        val openList = database.positionDao().getOpenPositions().firstOrNull() ?: emptyList()
        val posEntity = openList.find { it.id == positionId } ?: return Result.failure(Exception("Position not found"))
        val asset = _assets.value.find { it.symbol == posEntity.symbol }
        val closePrice = if (posEntity.side == OrderSide.BUY.name) (asset?.bid ?: posEntity.currentPrice) else (asset?.ask ?: posEntity.currentPrice)

        val multiplier = if (posEntity.side == OrderSide.BUY.name) 1 else -1
        val lotMultiplier = if (asset?.category == AssetCategory.FOREX) 100000.0 else 1.0
        val realizedPnL = (closePrice - posEntity.openPrice) * posEntity.volumeLots * lotMultiplier * multiplier

        val closedEntity = posEntity.copy(
            status = PositionStatus.CLOSED.name,
            closePrice = closePrice,
            closeTimestamp = System.currentTimeMillis(),
            realizedPnL = realizedPnL
        )

        database.positionDao().updatePosition(closedEntity)

        // Add transaction log
        val tx = TransactionEntity(
            id = "TX-CLO-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
            type = if (realizedPnL >= 0) TransactionType.TRADE_PROFIT.name else TransactionType.TRADE_LOSS.name,
            amount = realizedPnL,
            currency = "USD",
            method = PaymentMethod.BANK_TRANSFER.name,
            status = TransactionStatus.COMPLETED.name,
            timestamp = System.currentTimeMillis(),
            referenceHash = "MT5-CLOSE-${closedEntity.mt5Ticket}",
            fee = 0.0,
            accountDestination = "${posEntity.symbol} (${posEntity.side})",
            notes = "Closed Position #${closedEntity.mt5Ticket} at price $closePrice"
        )
        database.transactionDao().insertTransaction(tx)

        // Update profile
        _userProfile.update { profile ->
            val updatedBalance = profile.totalBalance + realizedPnL
            val updatedUsedMargin = (profile.usedMargin - posEntity.marginRequired).coerceAtLeast(0.0)
            val updatedEquity = updatedBalance
            val updatedFreeMargin = updatedEquity - updatedUsedMargin
            profile.copy(
                totalBalance = updatedBalance,
                equity = updatedEquity,
                usedMargin = updatedUsedMargin,
                freeMargin = updatedFreeMargin,
                todayPnl = profile.todayPnl + realizedPnL
            )
        }

        return Result.success(realizedPnL)
    }

    suspend fun processPayPalDeposit(amount: Double, payerEmail: String): Transaction {
        val txId = "PP-DEP-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val refHash = "PAYPAL-ORD-" + Random.nextInt(10000000, 99999999)
        val transaction = Transaction(
            id = txId,
            type = TransactionType.DEPOSIT,
            amount = amount,
            currency = "USD",
            method = PaymentMethod.PAYPAL,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
            referenceHash = refHash,
            fee = 0.0,
            accountDestination = "Payer: $payerEmail -> MT5 Trading Balance",
            notes = "Instant PayPal Capture API v2 processed. Verified with 256-bit SSL."
        )

        database.transactionDao().insertTransaction(transaction.toEntity())

        _userProfile.update { profile ->
            val newBalance = profile.totalBalance + amount
            val newEquity = profile.equity + amount
            val newFreeMargin = profile.freeMargin + amount
            profile.copy(
                totalBalance = newBalance,
                equity = newEquity,
                freeMargin = newFreeMargin
            )
        }

        val notif = NotificationEntity(
            id = "NOTIF-" + System.currentTimeMillis(),
            title = "Deposit Successful: PayPal",
            titleAr = "تم الإيداع بنجاح عبر باي بال",
            message = "Deposited $${String.format(Locale.US, "%,.2f", amount)} USD via PayPal ($payerEmail).",
            messageAr = "تم إيداع $${String.format(Locale.US, "%,.2f", amount)} دولار بنجاح عبر باي بال ($payerEmail).",
            type = "PAYMENT",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        database.notificationDao().insertNotification(notif)

        return transaction
    }

    suspend fun processWithdrawal(
        amount: Double,
        method: PaymentMethod,
        destination: String,
        notes: String = ""
    ): Result<Transaction> {
        if (amount <= 0) {
            return Result.failure(Exception("Withdrawal amount must be greater than zero."))
        }
        if (amount > _userProfile.value.freeMargin) {
            return Result.failure(Exception("Withdrawal exceeds free margin ($${_userProfile.value.freeMargin})"))
        }

        val txId = "WTH-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
        val refHash = "WTH-REF-" + Random.nextInt(10000000, 99999999)
        val initialNotes = if (notes.isNotBlank()) notes else "Dispatched to PayPal Sandbox Webhook verification & clearing protocol."
        val transaction = Transaction(
            id = txId,
            type = TransactionType.WITHDRAWAL,
            amount = amount,
            currency = "USD",
            method = method,
            status = TransactionStatus.PENDING,
            timestamp = System.currentTimeMillis(),
            referenceHash = refHash,
            fee = 0.0, // VIP 0% Wire fee promo
            accountDestination = destination,
            notes = initialNotes
        )

        database.transactionDao().insertTransaction(transaction.toEntity())

        // Trigger real-time background PayPal Webhook verification listener
        payPalWebhookService.trackWithdrawal(
            transactionId = txId,
            amount = amount,
            method = method.name,
            destination = destination
        )

        _userProfile.update { profile ->
            val newBalance = profile.totalBalance - amount
            val newEquity = profile.equity - amount
            val newFreeMargin = profile.freeMargin - amount
            profile.copy(
                totalBalance = newBalance,
                equity = newEquity,
                freeMargin = newFreeMargin
            )
        }

        val methodLabel = if (method == PaymentMethod.BANK_TRANSFER) "Bank Wire" else method.displayName
        val methodLabelAr = if (method == PaymentMethod.BANK_TRANSFER) "حوالة بنكية مباشرة" else method.displayName
        val notif = NotificationEntity(
            id = "NOTIF-" + System.currentTimeMillis(),
            title = "Cash Withdrawal Dispatched",
            titleAr = "تم تحويل طلب السحب النقدي بنجاح",
            message = "Withdrawal of $${String.format(Locale.US, "%,.2f", amount)} USD sent via $methodLabel to $destination (Awaiting Webhook Clearance).",
            messageAr = "تم إرسال سحب نقدي بمبلغ $${String.format(Locale.US, "%,.2f", amount)} دولار عبر $methodLabelAr إلى $destination (بانتظار توثيق الويب هوك).",
            type = "PAYMENT",
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        database.notificationDao().insertNotification(notif)

        return Result.success(transaction)
    }

    fun forceWebhookVerification(transactionId: String, destination: String, amount: Double) {
        payPalWebhookService.forceImmediateVerification(transactionId, destination, amount)
    }

    suspend fun markNotificationAsRead(id: String) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        database.notificationDao().markAllAsRead()
    }

    suspend fun getAiTradingAdvice(symbol: String, timeframe: String, isArabic: Boolean): AiTradingAdvice {
        val asset = _assets.value.find { it.symbol == symbol } ?: _assets.value.first()
        return aiTradingAgent.analyzeAssetAndGenerateAdvice(asset, timeframe, isArabic)
    }

    // Chart Candlesticks fetched directly from live financial data feeds
    suspend fun getCandlesticksForAsset(symbol: String, timeframe: String): List<CandleStick> {
        val currentPrice = _assets.value.find { it.symbol == symbol }?.price ?: 0.0
        return liveMarketService.fetchCandlesticks(symbol, timeframe, currentPrice)
    }

    fun getCandlesticksForAssetSync(symbol: String, timeframe: String): List<CandleStick> {
        val asset = _assets.value.find { it.symbol == symbol }
        val basePrice = asset?.price ?: 100.0
        val count = when (timeframe) {
            "1M" -> 30
            "5M" -> 35
            "1H" -> 40
            "1D" -> 50
            "1W" -> 52
            else -> 30
        }

        val now = System.currentTimeMillis()
        val intervalMs = when (timeframe) {
            "1M" -> 60000L
            "5M" -> 300000L
            "1H" -> 3600000L
            "1D" -> 86400000L
            "1W" -> 604800000L
            else -> 3600000L
        }

        val candles = mutableListOf<CandleStick>()
        val startPrice = (basePrice * 0.97).toFloat()
        var runningClose = startPrice

        for (i in count downTo 0) {
            val t = now - (i * intervalMs)
            val progress = (count - i).toFloat() / count.toFloat()
            val targetMean = startPrice + (basePrice.toFloat() - startPrice) * progress
            val wiggle = (sin(i * 0.45) * 0.005 * basePrice).toFloat()
            val open = runningClose
            val close = if (i == 0) basePrice.toFloat() else (targetMean + wiggle).coerceAtLeast(0.0001f)
            val high = maxOf(open, close) + (basePrice.toFloat() * 0.004f)
            val low = minOf(open, close) - (basePrice.toFloat() * 0.004f)
            val volume = (basePrice * 80.0).toFloat()

            candles.add(
                CandleStick(
                    timestamp = t,
                    open = open,
                    high = high,
                    low = maxOf(low, 0.0001f),
                    close = close,
                    volume = volume
                )
            )
            runningClose = close
        }
        return candles
    }

    // Initial asset catalogue - Global Multi-Asset Coverage
    private fun getInitialAssets(): List<Asset> {
        return listOf(
            Asset(
                symbol = "AAPL",
                name = "Apple Inc.",
                category = AssetCategory.STOCKS,
                price = 325.14,
                change24h = 3.85,
                changePercent24h = 1.20,
                high24h = 327.30,
                low24h = 314.74,
                volume = "53.2M",
                bid = 325.09,
                ask = 325.19,
                spreadPips = 1.0,
                mt5Symbol = "AAPL.us",
                marketCapOrSupply = "$4.75T",
                historicalPoints = listOf(314.5, 318.0, 321.2, 323.0, 324.5, 325.14),
                isFavorite = true
            ),
            Asset(
                symbol = "NVDA",
                name = "NVIDIA Corporation",
                category = AssetCategory.STOCKS,
                price = 217.50,
                change24h = 4.10,
                changePercent24h = 1.92,
                high24h = 220.50,
                low24h = 215.20,
                volume = "98.4M",
                bid = 217.45,
                ask = 217.55,
                spreadPips = 1.0,
                mt5Symbol = "NVDA.us",
                marketCapOrSupply = "$5.35T",
                historicalPoints = listOf(210.0, 212.5, 214.0, 215.8, 216.5, 217.50),
                isFavorite = true
            ),
            Asset(
                symbol = "TSLA",
                name = "Tesla Inc.",
                category = AssetCategory.STOCKS,
                price = 356.09,
                change24h = -11.80,
                changePercent24h = -3.21,
                high24h = 367.40,
                low24h = 352.00,
                volume = "62.1M",
                bid = 356.04,
                ask = 356.14,
                spreadPips = 1.0,
                mt5Symbol = "TSLA.us",
                marketCapOrSupply = "$1.41T",
                historicalPoints = listOf(368.0, 364.0, 360.5, 358.0, 355.2, 356.09),
                isFavorite = false
            ),
            Asset(
                symbol = "MSFT",
                name = "Microsoft Corporation",
                category = AssetCategory.STOCKS,
                price = 501.02,
                change24h = -6.20,
                changePercent24h = -1.22,
                high24h = 504.80,
                low24h = 498.20,
                volume = "24.5M",
                bid = 500.97,
                ask = 501.07,
                spreadPips = 1.0,
                mt5Symbol = "MSFT.us",
                marketCapOrSupply = "$3.75T",
                historicalPoints = listOf(508.0, 506.2, 504.0, 502.5, 501.02),
                isFavorite = true
            ),
            Asset(
                symbol = "AMZN",
                name = "Amazon.com Inc.",
                category = AssetCategory.STOCKS,
                price = 254.55,
                change24h = 2.10,
                changePercent24h = 0.83,
                high24h = 258.00,
                low24h = 251.20,
                volume = "35.8M",
                bid = 254.50,
                ask = 254.60,
                spreadPips = 1.0,
                mt5Symbol = "AMZN.us",
                marketCapOrSupply = "$2.75T",
                historicalPoints = listOf(248.0, 250.5, 252.0, 253.8, 254.55),
                isFavorite = false
            ),
            Asset(
                symbol = "EURUSD",
                name = "Euro / US Dollar",
                category = AssetCategory.FOREX,
                price = 1.08740,
                change24h = 0.0031,
                changePercent24h = 0.29,
                high24h = 1.0895,
                low24h = 1.0832,
                volume = "184K Lots",
                bid = 1.08735,
                ask = 1.08745,
                spreadPips = 0.8,
                mt5Symbol = "EURUSD",
                marketCapOrSupply = "FX Major",
                historicalPoints = listOf(1.0840, 1.0852, 1.0860, 1.0868, 1.0874),
                isFavorite = true
            ),
            Asset(
                symbol = "GBPUSD",
                name = "British Pound / US Dollar",
                category = AssetCategory.FOREX,
                price = 1.31250,
                change24h = -0.0018,
                changePercent24h = -0.14,
                high24h = 1.3160,
                low24h = 1.3095,
                volume = "142K Lots",
                bid = 1.31240,
                ask = 1.31260,
                spreadPips = 1.2,
                mt5Symbol = "GBPUSD",
                marketCapOrSupply = "FX Major",
                historicalPoints = listOf(1.3150, 1.3142, 1.3130, 1.3115, 1.3125),
                isFavorite = false
            ),
            Asset(
                symbol = "USDJPY",
                name = "US Dollar / Japanese Yen",
                category = AssetCategory.FOREX,
                price = 144.650,
                change24h = -0.85,
                changePercent24h = -0.58,
                high24h = 146.10,
                low24h = 144.20,
                volume = "196K Lots",
                bid = 144.640,
                ask = 144.660,
                spreadPips = 0.9,
                mt5Symbol = "USDJPY",
                marketCapOrSupply = "FX Major",
                historicalPoints = listOf(146.0, 145.5, 145.1, 144.8, 144.65),
                isFavorite = true
            ),
            Asset(
                symbol = "BTCUSD",
                name = "Bitcoin",
                category = AssetCategory.CRYPTO,
                price = 77643.00,
                change24h = -1350.00,
                changePercent24h = -1.71,
                high24h = 78900.00,
                low24h = 76800.00,
                volume = "$34.2B",
                bid = 77635.00,
                ask = 77650.00,
                spreadPips = 15.0,
                mt5Symbol = "BTCUSD.crypto",
                marketCapOrSupply = "19.8M BTC",
                historicalPoints = listOf(76200.0, 76800.0, 77200.0, 78100.0, 77643.0),
                isFavorite = true
            ),
            Asset(
                symbol = "ETHUSD",
                name = "Ethereum",
                category = AssetCategory.CRYPTO,
                price = 2408.00,
                change24h = -38.50,
                changePercent24h = -1.57,
                high24h = 2465.00,
                low24h = 2380.00,
                volume = "$16.5B",
                bid = 2407.40,
                ask = 2408.60,
                spreadPips = 1.2,
                mt5Symbol = "ETHUSD.crypto",
                marketCapOrSupply = "120.4M ETH",
                historicalPoints = listOf(2450.0, 2435.0, 2420.0, 2395.0, 2408.0),
                isFavorite = false
            ),
            Asset(
                symbol = "SOLUSD",
                name = "Solana",
                category = AssetCategory.CRYPTO,
                price = 100.24,
                change24h = 2.40,
                changePercent24h = 2.45,
                high24h = 103.50,
                low24h = 97.80,
                volume = "$3.9B",
                bid = 100.20,
                ask = 100.28,
                spreadPips = 0.8,
                mt5Symbol = "SOLUSD.crypto",
                marketCapOrSupply = "472M SOL",
                historicalPoints = listOf(96.0, 97.5, 98.8, 99.4, 100.24),
                isFavorite = true
            ),
            Asset(
                symbol = "XAUUSD",
                name = "Spot Gold",
                category = AssetCategory.COMMODITIES,
                price = 4313.00,
                change24h = 28.50,
                changePercent24h = 0.66,
                high24h = 4341.00,
                low24h = 4295.00,
                volume = "$14.6B",
                bid = 4312.65,
                ask = 4313.35,
                spreadPips = 7.0,
                mt5Symbol = "XAUUSD",
                marketCapOrSupply = "Precious Metal",
                historicalPoints = listOf(4260.0, 4280.0, 4295.0, 4305.0, 4313.0),
                isFavorite = true
            ),
            Asset(
                symbol = "XAGUSD",
                name = "Spot Silver",
                category = AssetCategory.COMMODITIES,
                price = 63.95,
                change24h = 0.95,
                changePercent24h = 1.51,
                high24h = 64.50,
                low24h = 63.10,
                volume = "$3.8B",
                bid = 63.93,
                ask = 63.97,
                spreadPips = 2.0,
                mt5Symbol = "XAGUSD",
                marketCapOrSupply = "Precious Metal",
                historicalPoints = listOf(62.0, 62.6, 63.1, 63.5, 63.95),
                isFavorite = false
            ),
            Asset(
                symbol = "USOIL",
                name = "Crude Oil (WTI)",
                category = AssetCategory.COMMODITIES,
                price = 74.85,
                change24h = -0.95,
                changePercent24h = -1.25,
                high24h = 76.10,
                low24h = 73.80,
                volume = "2.8M Contracts",
                bid = 74.80,
                ask = 74.90,
                spreadPips = 2.0,
                mt5Symbol = "USOIL.cash",
                marketCapOrSupply = "Energy",
                historicalPoints = listOf(76.5, 75.8, 75.2, 74.1, 74.85),
                isFavorite = false
            ),
            Asset(
                symbol = "SPX500",
                name = "S&P 500 Index",
                category = AssetCategory.INDICES,
                price = 6420.00,
                change24h = 42.80,
                changePercent24h = 0.67,
                high24h = 6455.00,
                low24h = 6395.00,
                volume = "3.8B Shares",
                bid = 6419.50,
                ask = 6420.50,
                spreadPips = 1.0,
                mt5Symbol = "US500.cash",
                marketCapOrSupply = "US Benchmark",
                historicalPoints = listOf(6340.0, 6370.0, 6390.0, 6405.0, 6420.0),
                isFavorite = true
            ),
            Asset(
                symbol = "NAS100",
                name = "Nasdaq 100 Index",
                category = AssetCategory.INDICES,
                price = 22850.00,
                change24h = 240.00,
                changePercent24h = 1.06,
                high24h = 23010.00,
                low24h = 22740.00,
                volume = "5.8B Shares",
                bid = 22848.50,
                ask = 22851.50,
                spreadPips = 1.5,
                mt5Symbol = "USTEC.cash",
                marketCapOrSupply = "Tech Index",
                historicalPoints = listOf(22500.0, 22620.0, 22710.0, 22780.0, 22850.0),
                isFavorite = true
            )
        )
    }

    private fun getInitialSignals(): List<TradingSignal> {
        return listOf(
            TradingSignal(
                id = "SIG-101",
                symbol = "NVDA",
                assetName = "NVIDIA Corporation",
                action = SignalAction.STRONG_BUY,
                timeframe = "1H / 4H",
                entryPrice = 131.20,
                targetPrice1 = 142.00,
                targetPrice2 = 155.00,
                stopLoss = 122.50,
                confidencePercent = 92,
                technicalIndicatorTrigger = "Bollinger Upper Squeeze + RSI 58 Bullish Momentum",
                analysisText = "Institutional accumulation detected. MT5 volume delta shows heavy net-long buyer absorption above key $128 support pivot.",
                analysisTextAr = "تم رصد تراكم مؤسسي ضخم. مؤشر دلتا الحجم في MT5 يظهر امتصاصاً شرائياً صافياً فوق مستوى الدعم المحوري 128 دولار.",
                timestamp = System.currentTimeMillis() - 1800000L
            ),
            TradingSignal(
                id = "SIG-102",
                symbol = "EURUSD",
                assetName = "Euro / US Dollar",
                action = SignalAction.BUY,
                timeframe = "15M / 1H",
                entryPrice = 1.0874,
                targetPrice1 = 1.0940,
                targetPrice2 = 1.0990,
                stopLoss = 1.0820,
                confidencePercent = 84,
                technicalIndicatorTrigger = "SMA 20/50 Golden Cross + MACD Zero-Line Breakout",
                analysisText = "ECB rate stability remarks fueling Euro strength against USD basket. Order flow depth indicates liquid stop-hunt cleared at 1.0830.",
                analysisTextAr = "تصريحات البنك المركزي الأوروبي تدعم قوة اليورو أمام الدولار. عمق تدفق الأوامر يشير لتجاوز مستويات السيولة عند 1.0830.",
                timestamp = System.currentTimeMillis() - 4200000L
            ),
            TradingSignal(
                id = "SIG-103",
                symbol = "BTCUSD",
                assetName = "Bitcoin",
                action = SignalAction.STRONG_BUY,
                timeframe = "4H / 1D",
                entryPrice = 63840.0,
                targetPrice1 = 66500.0,
                targetPrice2 = 70000.0,
                stopLoss = 61500.0,
                confidencePercent = 89,
                technicalIndicatorTrigger = "On-Chain Outflow + Inverse Head & Shoulders Breakout",
                analysisText = "Miners and ETF custody inflows spike to 3-week highs. Breaking above 64k opens the path to ATH retest.",
                analysisTextAr = "ارتفاع تدفقات صناديق الاستثمار المتداولة ETF إلى أعلى مستوى في 3 أسابيع. اختراق حاجز 64 ألف يفتح المجال لاختبار القمة التاريخية.",
                timestamp = System.currentTimeMillis() - 7200000L
            ),
            TradingSignal(
                id = "SIG-104",
                symbol = "USOIL",
                assetName = "Crude Oil (WTI)",
                action = SignalAction.SELL,
                timeframe = "1H",
                entryPrice = 74.60,
                targetPrice1 = 72.00,
                targetPrice2 = 69.50,
                stopLoss = 76.50,
                confidencePercent = 78,
                technicalIndicatorTrigger = "Overbought RSI (74) + Lower High Candlestick Rejection",
                analysisText = "Inventory build reported in API data. Resistance at $76.20 holding firm.",
                analysisTextAr = "تراكم مخزونات النفط حسب بيانات API. مقاومة قوية عند مستوى 76.20 دولار تمنع الصعود.",
                timestamp = System.currentTimeMillis() - 10800000L
            )
        )
    }

    private fun getInitialNews(): List<MarketNews> {
        val now = System.currentTimeMillis()
        return listOf(
            MarketNews(
                id = "NEWS-1",
                title = "NVIDIA Announces Next-Gen Blackwell Architecture Volume Shipments Ahead of Schedule",
                titleAr = "إنفيديا تعلن بدء شحن معالجات بلاكويل للذكاء الاصطناعي قبل الموعد المحدد",
                summary = "Hyperscale cloud providers expand advance allocations by $18B as gross margins remain above 75%.",
                summaryAr = "مزودو الحوسبة السحابية الكبرى يوسعون طلبياتهم المسبقة بقيمة 18 مليار دولار مع بقاء هوامش الربح فوق 75%.",
                source = "Bloomberg Terminal",
                timeAgo = "6m ago",
                timeAgoAr = "منذ 6 دقائق",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "NVDA",
                timestamp = now - 360000L,
                expectedImpactPercent = 4.2,
                impactProbability = 94,
                stockImpactAnalysis = "Strong bullish catalyst: Immediate institutional volume surge expected to push price past the $220 resistance ceiling toward $228.",
                stockImpactAnalysisAr = "محفز صعودي قوي: تدفق سيولة مؤسسية لحظية متوقع أن يدفع السهم لاختراق مقاومة 220$ والتوجه نحو 228$."
            ),
            MarketNews(
                id = "NEWS-2",
                title = "Apple Unveils On-Device Generative AI Hardware Upgrades with New Silicon",
                titleAr = "آبل تكشف عن ترقيات معالجات السيليكون المخصصة للذكاء الاصطناعي التوليدي",
                summary = "Morgan Stanley raises AAPL price target to $345 citing unprecedented iPhone replacement supercycle.",
                summaryAr = "مورغان ستانلي يرفع السعر المستهدف لسهم آبل إلى 345 دولاراً متوقعاً دورة ترقية قياسية لأجهزة آيفون.",
                source = "Wall Street Journal",
                timeAgo = "12m ago",
                timeAgoAr = "منذ 12 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "AAPL",
                timestamp = now - 720000L,
                expectedImpactPercent = 2.8,
                impactProbability = 89,
                stockImpactAnalysis = "Bullish momentum: Expansion in forward hardware margins and strong retail demand support upward re-rating.",
                stockImpactAnalysisAr = "زخم صعودي: توسع هوامش أرباح العتاد والطلب الاستهلاكي القوي يدعمان صعود السهم نحو قمم تاريخية جديدة."
            ),
            MarketNews(
                id = "NEWS-3",
                title = "Tesla Full Self-Driving V13 Regulatory Approval Fast-Tracked in Key Markets",
                titleAr = "تسريع الموافقات التنظيمية لمنظومة القيادة الذاتية الكاملة FSD V13 لتسلا",
                summary = "European and Asian transport authorities review commercial robotaxi autonomous deployment framework.",
                summaryAr = "هيئات النقل الأوروبية والآسيوية تراجع الإطار التشريعي لتشغيل أساطيل التاكسي الآلي التجاري.",
                source = "Reuters Market",
                timeAgo = "25m ago",
                timeAgoAr = "منذ 25 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "TSLA",
                timestamp = now - 1500000L,
                expectedImpactPercent = 5.6,
                impactProbability = 86,
                stockImpactAnalysis = "High Volatility Bullish: Short-covering rally anticipated above $360 with $375 as primary Fibonacci extension.",
                stockImpactAnalysisAr = "تقلبات صاعدة قوية: تغطية مراكز البيع المكشوف قد تدفع السهم لتجاوز 360$ واستهداف 375$ كهدف فيبوناتشي رئيسي."
            ),
            MarketNews(
                id = "NEWS-4",
                title = "Microsoft Azure AI Revenue Surpasses $14B Annualized Run-Rate",
                titleAr = "إيرادات مايكروسوفت أزور للذكاء الاصطناعي تتجاوز 14 مليار دولار بمعدل سنوي",
                summary = "Enterprise Copilot seat activations surge 60% quarter-over-quarter across Fortune 500 companies.",
                summaryAr = "تراخيص مساعد كوبايلوت للشركات ترتفع بنسبة 60% فصلياً بين كبرى شركات فورتشن 500.",
                source = "Financial Times",
                timeAgo = "38m ago",
                timeAgoAr = "منذ 38 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "MSFT",
                timestamp = now - 2280000L,
                expectedImpactPercent = 2.4,
                impactProbability = 91,
                stockImpactAnalysis = "Sustained Bullish: Steady recurring cash flows consolidate support at $498 and favor long positions targeting $515.",
                stockImpactAnalysisAr = "صعود مستدام: التدفقات النقدية المتكررة تعزز أرضية الدعم عند 498$ وتدعم استهداف مستوى 515$."
            ),
            MarketNews(
                id = "NEWS-5",
                title = "Spot Gold Rallies Past Historic Milestone as Global Central Banks Bolster Reserves",
                titleAr = "الذهب يسجل مستويات تاريخية قياسية مع تسارع مشتريات البنوك المركزية العالمية",
                summary = "Sovereign reserves add 140 tonnes in Q3 while US 10-Year Treasury real yields retreat.",
                summaryAr = "البنوك المركزية تضيف 140 طناً من الذهب في الربع الثالث مع تراجع عوائد السندات الأمريكية الحقيقية.",
                source = "Bloomberg Commodities",
                timeAgo = "45m ago",
                timeAgoAr = "منذ 45 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "XAUUSD",
                timestamp = now - 2700000L,
                expectedImpactPercent = 1.6,
                impactProbability = 95,
                stockImpactAnalysis = "High Probability Bullish Breakout: Institutional safe-haven hedging establishes solid floor at $2,490 aiming for $2,550.",
                stockImpactAnalysisAr = "اختراق صعودي عالي الاحتمالية: تحوط المؤسسات بالذهب يؤسس دعماً صلباً عند 2,490$ مستهدفاً 2,550$ للأونصة."
            ),
            MarketNews(
                id = "NEWS-6",
                title = "Bitcoin Institutional Spot ETF Daily Inflows Cross $480 Million",
                titleAr = "صافي التدفقات اليومية لصناديق بيتكوين الفورية ETF يتجاوز 480 مليون دولار",
                summary = "BlackRock and Fidelity vehicles absorb available OTC supply as exchange liquidity drops to multi-year lows.",
                summaryAr = "صناديق بلاك روك وفيديليتي تستحوذ على المعروض خارج المنصات مع هبوط سيولة التداول لأدنى مستوى في سنوات.",
                source = "CoinDesk Institutional",
                timeAgo = "52m ago",
                timeAgoAr = "منذ 52 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "BTCUSD",
                timestamp = now - 3120000L,
                expectedImpactPercent = 3.9,
                impactProbability = 88,
                stockImpactAnalysis = "Bullish Squeeze: Exchange supply crunch suggests continuation toward $66,000 with strong support at $62,500.",
                stockImpactAnalysisAr = "ضغط شرائي صاعد: شح المعروض في المنصات يدعم استمرار الصعود نحو 66,000$ مع دعم قوي عند 62,500$."
            ),
            MarketNews(
                id = "NEWS-7",
                title = "Crude Oil Pressured by US Inventory Surplus & Soft European Industrial PMI",
                titleAr = "النفط تحت ضغط بيعي إثر زيادة غير متوقعة في المخزونات الأمريكية وتباطؤ التصنيع",
                summary = "EIA weekly report reveals +3.4M barrel build, testing key technical support levels.",
                summaryAr = "تقرير إدارة الطاقة يظهر زيادة 3.4 مليون برميل مما يضع المستويات الفنية الداعمة تحت الاختبار.",
                source = "Reuters Energy",
                timeAgo = "1h ago",
                timeAgoAr = "منذ ساعة",
                sentiment = MarketSentiment.BEARISH,
                impact = NewsImpact.MEDIUM,
                relatedSymbol = "USOIL",
                timestamp = now - 3600000L,
                expectedImpactPercent = -2.1,
                impactProbability = 82,
                stockImpactAnalysis = "Bearish Correction: Short-term inventory overhang exerts downward pressure targeting $72.80 support.",
                stockImpactAnalysisAr = "تصحيح هبوطي: وفرة المعروض اللحظي تشكل ضغطاً هبوطياً يستهدف كسر مستوى الدعم 72.80$."
            ),
            MarketNews(
                id = "NEWS-8",
                title = "Amazon Web Services Signs Major Enterprise Cloud & AI Infrastructure Deals",
                titleAr = "أمازون كلاود توقع عقوداً مليارية جديدة للبنية السحابية وأنظمة الذكاء الاصطناعي",
                summary = "AWS revenue growth accelerates to 21% YoY, strengthening profitability outlook for retail and cloud segments.",
                summaryAr = "تسارع نمو إيرادات AWS إلى 21% سنوياً مما يعزز التوقعات الربحية لقطاعي التجارة والسحابة.",
                source = "CNBC Markets",
                timeAgo = "1h 15m ago",
                timeAgoAr = "منذ ساعة و15 دقيقة",
                sentiment = MarketSentiment.BULLISH,
                impact = NewsImpact.HIGH,
                relatedSymbol = "AMZN",
                timestamp = now - 4500000L,
                expectedImpactPercent = 3.1,
                impactProbability = 90,
                stockImpactAnalysis = "Bullish Expansion: Operating profit margins in cloud segment provide strong tailwind targeting $265.",
                stockImpactAnalysisAr = "توسع صعودي: ارتفاع هوامش الربح التشغيلي للسحابة يوفر دفعة قوية لاستهداف مستوى 265$."
            )
        )
    }

    private fun getInitialEconomicEvents(): List<EconomicEvent> {
        return listOf(
            EconomicEvent(
                id = "EVENT-01",
                title = "US FOMC Interest Rate Decision & Press Conference",
                titleAr = "قرار الفائدة الصادر عن الفيدرالي الأمريكي ومؤتمر باول",
                countryCode = "US",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = "Wednesday, 18:00 GMT",
                scheduledTimeAr = "الأربعاء، الساعة 18:00 بتوقيت غرينتش",
                forecast = "5.25%",
                previous = "5.50%",
                actual = null,
                affectedAssets = listOf("SPX500", "NAS100", "EURUSD", "XAUUSD", "BTCUSD"),
                expectedVolatility = "Extremely High (150-250 pips / 2.5% on Indices)",
                expectedVolatilityAr = "عالية جداً (150-250 نقطة / 2.5% على المؤشرات)",
                analysisNote = "Markets price a 94% probability of a 25bps rate cut. Dovish Powell rhetoric could spark explosive bull rallies across equities and bullion.",
                analysisNoteAr = "الأسواق تسعر احتمالية 94% لخفض الفائدة بمقدار 25 نقطة أساس. لهجة باول التيسيرية قد تشعل صعوداً صاروخياً للأسهم والذهب."
            ),
            EconomicEvent(
                id = "EVENT-02",
                title = "US Consumer Price Index (CPI YoY)",
                titleAr = "مؤشر أسعار المستهلك الأمريكي (التضخم السنوي CPI)",
                countryCode = "US",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = "Thursday, 12:30 GMT",
                scheduledTimeAr = "الخميس، الساعة 12:30 بتوقيت غرينتش",
                forecast = "2.9%",
                previous = "3.2%",
                actual = null,
                affectedAssets = listOf("EURUSD", "USDJPY", "AAPL", "NVDA", "XAUUSD"),
                expectedVolatility = "High Volatility (80-120 pips)",
                expectedVolatilityAr = "تقلبات مرتفعة (80-120 نقطة)",
                analysisNote = "A reading below 3.0% confirms disinflation path, accelerating dollar sell-offs and boosting gold towards $2,550.",
                analysisNoteAr = "قراءة أدنى من 3.0% تؤكد انحسار التضخم وتسرع عمليات بيع الدولار مما يدعم صعود الذهب نحو 2,550 دولار."
            ),
            EconomicEvent(
                id = "EVENT-03",
                title = "US Non-Farm Payrolls (NFP) & Unemployment Rate",
                titleAr = "تقرير الوظائف غير الزراعية الأمريكي ومعدل البطالة (NFP)",
                countryCode = "US",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = "Friday, 12:30 GMT",
                scheduledTimeAr = "الجمعة، الساعة 12:30 بتوقيت غرينتش",
                forecast = "165K",
                previous = "114K",
                actual = null,
                affectedAssets = listOf("SPX500", "USDJPY", "EURUSD", "XAUUSD"),
                expectedVolatility = "Very High Volatility",
                expectedVolatilityAr = "تقلبات عنيفة ومفاجئة",
                analysisNote = "Employment cooling indicates Fed will prioritize labor health over restrictive monetary policy.",
                analysisNoteAr = "تباطؤ وتيرة التوظيف يشير إلى أن الفيدرالي سيركز على حماية سوق العمل بخفض أسرع للفائدة."
            ),
            EconomicEvent(
                id = "EVENT-04",
                title = "OPEC+ Ministerial JMMC Output Quota Meeting",
                titleAr = "اجتماع اللجنة الوزارية لمنظمة أوبك+ لتحديد حصص إنتاج النفط",
                countryCode = "OPEC",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = "Sunday, 14:00 GMT",
                scheduledTimeAr = "الأحد، الساعة 14:00 بتوقيت غرينتش",
                forecast = "Voluntary Cut Extension",
                previous = "2.2M bpd cut",
                actual = null,
                affectedAssets = listOf("USOIL", "EURUSD", "CADUSD"),
                expectedVolatility = "High ($2.5 - $4.0/barrel swing)",
                expectedVolatilityAr = "مرتفعة (تذبذب 2.5 إلى 4 دولارات للبرميل)",
                analysisNote = "Decision on delaying voluntary supply phase-in will set the floor for WTI crude oil prices near $72-$78.",
                analysisNoteAr = "القرار بشأن تمديد التخفيضات الطوعية سيحدد أرضية أسعار خام غرب تكساس بين 72 إلى 78 دولاراً."
            ),
            EconomicEvent(
                id = "EVENT-05",
                title = "NVIDIA & Mega-Cap Tech Earnings Releases",
                titleAr = "إعلان الأرباح الفصلية لشركة إنفيديا وعمالقة التكنولوجيا",
                countryCode = "US",
                currency = "USD",
                impact = NewsImpact.HIGH,
                scheduledTime = "Next Tuesday, 20:05 GMT",
                scheduledTimeAr = "الثلاثاء القادم، الساعة 20:05 بتوقيت غرينتش",
                forecast = "EPS $0.68 / Rev $28.7B",
                previous = "EPS $0.61 / Rev $26.0B",
                actual = null,
                affectedAssets = listOf("NVDA", "NAS100", "MSFT", "AAPL", "SOLUSD"),
                expectedVolatility = "Mega Move (6-10% single stock swing)",
                expectedVolatilityAr = "تحركات حادة (6-10% تذبذب في السهم)",
                analysisNote = "Data center revenue growth and Blackwell architecture delivery timeline are the primary market bellwether.",
                analysisNoteAr = "نمو إيرادات مراكز البيانات والجدول الزمني لرقائق بلاكويل هما المحرك الرئيسي لسوق الأسهم العالمي."
            )
        )
    }

    private fun getInitialMarketMovers(): List<MarketMoverAnalysis> {
        return listOf(
            MarketMoverAnalysis(
                id = "MOVER-01",
                symbol = "NVDA",
                name = "NVIDIA Corporation",
                changePercent = 5.38,
                isRise = true,
                category = CatalystCategory.AI_TECH_BREAKTHROUGH,
                headline = "Surged +5.38% on Blackwell AI Superchip Production Ramp & Hyperscaler Orders",
                headlineAr = "ارتفاع قوي بنسبة +5.38% بفضل تسارع إنتاج رقائق بلاكويل وتدفق طلبيات الحوسبة السحابية",
                deepAnalysis = "Demand for next-gen Blackwell GPU architecture exceeded supply allocations through Q4 2026. Microsoft, Meta, and Google expanded data center capex spending, guaranteeing multi-billion forward revenues.",
                deepAnalysisAr = "تجاوز الطلب على معالجات بلاكويل للذكاء الاصطناعي كامل القدرة الإنتاجية المخصصة حتى الربع الرابع 2026. قيام مايكروسوفت وميتا وجوجل بزيادة إنفاقها الرأسمالي يضمن مليارات الدولارات من الإيرادات المستقبلية المسبقة.",
                keyPriceLevels = "Support: $124.30 | Resistance: $135.00 | All-Time High Target: $148.00",
                keyPriceLevelsAr = "مستوى الدعم: 124.30$ | المقاومة: 135.00$ | الهدف التاريخي: 148.00$",
                marketReactionTime = "Intraday Volume Spike: 112.5M shares",
                marketReactionTimeAr = "قفزة في حجم التداول اليومي: 112.5 مليون سهم"
            ),
            MarketMoverAnalysis(
                id = "MOVER-02",
                symbol = "XAUUSD",
                name = "Spot Gold",
                changePercent = 1.45,
                isRise = true,
                category = CatalystCategory.CENTRAL_BANK,
                headline = "Gold Rallied to Historic All-Time High Above $2,514 on Central Bank Reserves & Rate Cut Bets",
                headlineAr = "الذهب يسجل رقماً قياسياً تاريخياً فوق 2,514$ بدعم مشتريات البنوك المركزية وخفض الفائدة",
                deepAnalysis = "Sovereign central banks (PBoC, RBI, CBRT) added 420+ tonnes of physical gold to foreign reserves. Simultaneously, falling US 10-year Treasury yields reduced the opportunity cost of holding non-yielding bullion, triggering massive long institutional breakout orders.",
                deepAnalysisAr = "قامت البنوك المركزية الكبرى بإضافة أكثر من 420 طناً من الذهب الفعلي لاحتياطياتها السيادية. وتزامن ذلك مع هبوط عوائد السندات الأمريكية لأجل 10 سنوات مما قلص كلفة الفرصة البديلة ودفع صناديق التحوط لتنفيذ أوامر شراء ضخمة.",
                keyPriceLevels = "Support: $2,485.00 | Immediate Pivot: $2,500.00 | Bull Target: $2,580.00",
                keyPriceLevelsAr = "مستوى الدعم: 2,485$ | النقطة المحورية: 2,500$ | الهدف الصاعد: 2,580$",
                marketReactionTime = "Global COMEX Delivery Volume: +38% YoY",
                marketReactionTimeAr = "حجم تسليم عقود كومكس العالمية: +38% سنوياً"
            ),
            MarketMoverAnalysis(
                id = "MOVER-03",
                symbol = "USOIL",
                name = "Crude Oil (WTI)",
                changePercent = -1.58,
                isRise = false,
                category = CatalystCategory.GEOPOLITICS_SUPPLY,
                headline = "Plunged -1.58% to $74.60 Following EIA Crude Inventory Build & Soft Manufacturing Data",
                headlineAr = "هبوط بنسبة -1.58% إلى 74.60$ إثر ارتفاع مفاجئ في المخزونات الأمريكية وتباطؤ مؤشرات التصنيع",
                deepAnalysis = "EIA weekly report showed a surprise build of +3.4 million barrels against expected drawdown of -1.8M. Weakening PMI factory data in major consumer economies dampened immediate crack-spread demand, pushing hedge funds to take profits at the $76.20 resistance ceiling.",
                deepAnalysisAr = "أظهر تقرير إدارة معلومات الطاقة الأمريكية (EIA) زيادة مفاجئة في المخزونات بلغت +3.4 مليون برميل مقارنة بتوقعات السحب البالغة -1.8 مليون. كما أثر تباطؤ بيانات مديري المشتريات الصناعية سلباً على الطلب مما دفع صناديق التحوط لجني الأرباح.",
                keyPriceLevels = "Support: $72.50 | 200 EMA Resistance: $76.80 | Breakdown Level: $70.00",
                keyPriceLevelsAr = "مستوى الدعم: 72.50$ | مقاومة المتوسط 200: 76.80$ | كسر القاع: 70.00$",
                marketReactionTime = "Settlement Price Action: -3.8% from weekly peak",
                marketReactionTimeAr = "حركة سعر التسوية: -3.8% من قمة الأسبوع"
            ),
            MarketMoverAnalysis(
                id = "MOVER-04",
                symbol = "BTCUSD",
                name = "Bitcoin",
                changePercent = 2.97,
                isRise = true,
                category = CatalystCategory.CRYPTO_INSTITUTIONAL,
                headline = "Surged +2.97% Breaking $63,800 on $450M Spot ETF Net Inflows",
                headlineAr = "صعود بنسبة +2.97% وتجاوز 63,800$ مدفوعاً بتدفقات صافية بلغت 450 مليون دولار في صناديق ETF",
                deepAnalysis = "Institutional allocators channeled $450 million in daily net inflows into BlackRock IBIT and Fidelity FBTC spot ETFs. Exchange reserves plummeted to multi-year lows, triggering a short squeeze in perpetual futures funding rates.",
                deepAnalysisAr = "ضخ المستثمرون المؤسسيون 450 مليون دولار كصافي تدفقات يومية في صناديق بلاك روك وفيديليتي للبيتكوين. انخفاض معروض البيتكوين في المنصات لأدنى مستوى في عدة سنوات تسبب في تصفية عقود البيع على المكشوف (Short Squeeze).",
                keyPriceLevels = "Support Pivot: $61,500 | 50 EMA: $62,400 | ATH Retest Target: $68,500",
                keyPriceLevelsAr = "نقطة الدعم: 61,500$ | المتوسط 50: 62,400$ | هدف القمة التاريخية: 68,500$",
                marketReactionTime = "Liquidated Short Positions: $85M in 4 hours",
                marketReactionTimeAr = "تصفية مراكز بيع مكشوفة: 85 مليون دولار في 4 ساعات"
            ),
            MarketMoverAnalysis(
                id = "MOVER-05",
                symbol = "TSLA",
                name = "Tesla Inc.",
                changePercent = -1.71,
                isRise = false,
                category = CatalystCategory.EARNINGS_SURPRISE,
                headline = "Retraced -1.71% as Automotive Gross Margins Compress Amid Global EV Price Adjustments",
                headlineAr = "تراجع بنسبة -1.71% لتراجع هوامش أرباح السيارات في ظل تخفيضات الأسعار والمنافسة العالمية",
                deepAnalysis = "Investors reassessed near-term cash flows ahead of Robotaxi unveiling event. Automotive gross margin excluding regulatory credits stabilized at 14.6%, creating consolidation channel between $215 and $226.",
                deepAnalysisAr = "قام المستثمرون بإعادة تقييم التدفقات النقدية القريبة قبل حدث الكشف عن سيارات التاكسي الآلي (Robotaxi). استقرار هامش أرباح السيارات عند 14.6% أدى لتكوين قناة تداول عرضية بين 215$ و226$.",
                keyPriceLevels = "Support Floor: $212.00 | Resistance: $228.00 | Breakout Trigger: $235.00",
                keyPriceLevelsAr = "قاع الدعم: 212.00$ | المقاومة: 228.00$ | إشارة الاختراق: 235.00$",
                marketReactionTime = "Pre-market Block Orders: 4.8M shares exchanged",
                marketReactionTimeAr = "أوامر الصفقات الخاصة قبل الافتتاح: 4.8 مليون سهم"
            )
        )
    }
}

// Mapper extension helpers
private fun PositionEntity.toPosition() = Position(
    id = id,
    symbol = symbol,
    assetName = assetName,
    side = OrderSide.valueOf(side),
    volumeLots = volumeLots,
    openPrice = openPrice,
    currentPrice = currentPrice,
    stopLoss = stopLoss,
    takeProfit = takeProfit,
    floatingPnL = floatingPnL,
    marginRequired = marginRequired,
    leverage = leverage,
    openTimestamp = openTimestamp,
    closeTimestamp = closeTimestamp,
    closePrice = closePrice,
    realizedPnL = realizedPnL,
    status = PositionStatus.valueOf(status),
    mt5Ticket = mt5Ticket
)

private fun Position.toEntity() = PositionEntity(
    id = id,
    symbol = symbol,
    assetName = assetName,
    side = side.name,
    volumeLots = volumeLots,
    openPrice = openPrice,
    currentPrice = currentPrice,
    stopLoss = stopLoss,
    takeProfit = takeProfit,
    floatingPnL = floatingPnL,
    marginRequired = marginRequired,
    leverage = leverage,
    openTimestamp = openTimestamp,
    closeTimestamp = closeTimestamp,
    closePrice = closePrice,
    realizedPnL = realizedPnL,
    status = status.name,
    mt5Ticket = mt5Ticket
)

private fun TransactionEntity.toTransaction() = Transaction(
    id = id,
    type = TransactionType.valueOf(type),
    amount = amount,
    currency = currency,
    method = PaymentMethod.valueOf(method),
    status = TransactionStatus.valueOf(status),
    timestamp = timestamp,
    referenceHash = referenceHash,
    fee = fee,
    accountDestination = accountDestination,
    notes = notes
)

private fun Transaction.toEntity() = TransactionEntity(
    id = id,
    type = type.name,
    amount = amount,
    currency = currency,
    method = method.name,
    status = status.name,
    timestamp = timestamp,
    referenceHash = referenceHash,
    fee = fee,
    accountDestination = accountDestination,
    notes = notes
)

private fun NotificationEntity.toNotificationItem() = NotificationItem(
    id = id,
    title = title,
    titleAr = titleAr,
    message = message,
    messageAr = messageAr,
    type = NotificationType.valueOf(type),
    timestamp = timestamp,
    isRead = isRead
)

private fun NotificationItem.toEntity() = NotificationEntity(
    id = id,
    title = title,
    titleAr = titleAr,
    message = message,
    messageAr = messageAr,
    type = type.name,
    timestamp = timestamp,
    isRead = isRead
)
