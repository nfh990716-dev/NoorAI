package com.noor.salhi.engine

/** بيانات طلب واحد كما تم قراءتها من الشاشة أو أدخلها المستخدم. */
data class Ride(
    val fare: Double,
    val pickupKm: Double,
    val tripKm: Double,
    val minutes: Int,
    val appName: String? = null,
    /** درجة ثقة القراءة من 0.0 إلى 1.0 */
    val confidence: Double = 1.0,
) {
    val totalKm: Double get() = pickupKm + tripKm
}

/** تكلفة واحدة قابلة للتشغيل والإيقاف بشكل مستقل. */
data class Expense(
    val id: String,
    val name: String,
    val enabled: Boolean = true,
    val perKm: Double = 0.0,
    val perRide: Double = 0.0,
)

/** ملف تكاليف كامل، ولكل ملف إعداداته الخاصة. */
data class CostProfile(
    val name: String,
    val commissionPercent: Double = 20.0,
    val fuelEnabled: Boolean = true,
    /** تكلفة البنزين لكل 100 كم بالدينار العراقي */
    val fuelPer100Km: Double = 6500.0,
    val expenses: List<Expense> = defaultExpenses(),
    val minNetProfit: Double = 0.0,
    val minFarePerKm: Double = 0.0,
    val maxTotalKm: Double? = null,
    val maxMinutes: Int? = null,
) {
    companion object {
        fun defaultExpenses() = listOf(
            Expense("maintenance", "الصيانة", perKm = 30.0),
            Expense("tires", "الإطارات", perKm = 20.0),
            Expense("depreciation", "استهلاك السيارة", perKm = 60.0),
            Expense("reserve", "المصاريف الاحتياطية", perKm = 20.0),
        )
    }
}

enum class Rating(val label: String) {
    BAD("سيء"),
    WEAK("ضعيف"),
    AVERAGE("متوسط"),
    GOOD("جيد"),
    EXCELLENT("ممتاز"),
}

/** نتيجة تحليل طلب واحد. كل الأرقام مبنية على الإعدادات المفعّلة فقط. */
data class RideAnalysis(
    val fare: Double,
    val totalKm: Double,
    val commission: Double,
    val fuelCost: Double,
    val otherExpenses: Double,
    val netProfit: Double,
    val farePerKm: Double,
    val netPerKm: Double,
    val netPerHour: Double?,
    val rating: Rating,
    val reliable: Boolean,
    val reasons: List<String>,
)
