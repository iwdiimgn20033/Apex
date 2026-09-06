package com.example.data.model

data class LiveMarketStatus(
    val isConnected: Boolean = true,
    val isDirectInternet: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val latencyMs: Int = 24,
    val providerName: String = "Binance + ECB Interbank Live FX",
    val activeSymbolCount: Int = 12,
    val errorMessage: String? = null
)
