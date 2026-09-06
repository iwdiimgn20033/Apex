package com.example.data.model

data class EconomicEvent(
    val id: String,
    val title: String,
    val titleAr: String,
    val countryCode: String,
    val currency: String,
    val impact: NewsImpact,
    val scheduledTime: String,
    val scheduledTimeAr: String,
    val forecast: String,
    val previous: String,
    val actual: String? = null,
    val affectedAssets: List<String>,
    val expectedVolatility: String,
    val expectedVolatilityAr: String,
    val analysisNote: String,
    val analysisNoteAr: String
)
