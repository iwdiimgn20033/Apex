package com.example.data.model

enum class KycTier(val label: String, val limitUsd: String) {
    TIER_1("Tier 1 - Standard", "$10,000 / day"),
    TIER_2("Tier 2 - Verified Pro", "$100,000 / day"),
    TIER_3("Tier 3 - Institutional", "Unlimited")
}

enum class KycStatus(val labelEn: String, val labelAr: String) {
    VERIFIED("Verified", "تم التحقق"),
    PENDING("Pending Review", "قيد المراجعة"),
    UNVERIFIED("Action Required", "مطلوب التحقق")
}

enum class UserRole {
    TRADER, COMPLIANCE_ADMIN
}

data class UserProfile(
    val userId: String = "USR-89421",
    val fullName: String = "Alexander Vance",
    val email: String = "alex.vance@apexbroker.com",
    val phoneNumber: String = "+1 (555) 234-8921",
    val kycTier: KycTier = KycTier.TIER_2,
    val kycStatus: KycStatus = KycStatus.VERIFIED,
    val role: UserRole = UserRole.TRADER,
    val twoFactorEnabled: Boolean = true,
    val biometricEnabled: Boolean = true,
    val mt5AccountNumber: String = "MT5-4820199",
    val mt5Server: String = "ApexBroker-Live-01",
    val leverageAllowed: Int = 100,
    val baseCurrency: String = "USD",
    val totalBalance: Double = 54820.50,
    val equity: Double = 56210.80,
    val usedMargin: Double = 4250.00,
    val freeMargin: Double = 51960.80,
    val marginLevelPercent: Double = 1322.6,
    val todayPnl: Double = 1390.30,
    val todayPnlPercent: Double = 2.54,
    val allTimeReturnPercent: Double = 44.8
)
