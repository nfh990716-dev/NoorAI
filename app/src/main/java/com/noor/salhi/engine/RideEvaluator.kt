package com.noor.salhi.engine

/**
 * محرك تحليل الطلبات. لا يعتمد على Android، لذلك يمكن اختباره بـ JUnit مباشرة.
 */
object RideEvaluator {

    /** الحدود الافتراضية لتصنيف الأجرة لكل كم (بالدينار). */
    const val FARE_PER_KM_WEAK = 250.0
    const val FARE_PER_KM_AVERAGE = 300.0
    const val FARE_PER_KM_GOOD = 350.0

    /** الحد الأدنى لدرجة الثقة لاعتبار الطلب موثوقاً للقرار الآلي. */
    const val MIN_RELIABLE_CONFIDENCE = 0.6

    fun analyze(ride: Ride, profile: CostProfile): RideAnalysis {
        val km = ride.totalKm
        val commission = ride.fare * profile.commissionPercent / 100.0
        val fuel = if (profile.fuelEnabled) km * profile.fuelPer100Km / 100.0 else 0.0
        val others = profile.expenses
            .filter { it.enabled }
            .sumOf { it.perKm * km + it.perRide }
        val net = ride.fare - commission - fuel - others

        val farePerKm = if (km > 0) ride.fare / km else 0.0
        val netPerKm = if (km > 0) net / km else 0.0
        val netPerHour = if (ride.minutes > 0) net / (ride.minutes / 60.0) else null

        val reasons = mutableListOf<String>()
        var rating = ratingByFarePerKm(farePerKm)

        // لا يُصنَّف الطلب بأعلى من ضعيف إذا كان صافي الربح لا يحقق الحد الأدنى
        if (net < profile.minNetProfit) {
            rating = minOf(rating, Rating.WEAK, compareBy { it.ordinal })
            reasons += "صافي الربح أقل من الحد الأدنى"
        }
        if (net <= 0) {
            rating = Rating.BAD
            reasons += "الطلب خاسر بعد احتساب التكاليف"
        }
        if (profile.minFarePerKm > 0 && farePerKm < profile.minFarePerKm) {
            reasons += "الأجرة لكل كم أقل من حد ملفك"
        }
        if (profile.maxTotalKm != null && km > profile.maxTotalKm) {
            reasons += "المسافة الإجمالية تتجاوز حد ملفك"
        }
        if (profile.maxMinutes != null && ride.minutes > profile.maxMinutes) {
            reasons += "الوقت يتجاوز حد ملفك"
        }

        val reliable = ride.confidence >= MIN_RELIABLE_CONFIDENCE && km > 0
        if (!reliable) {
            reasons += "بيانات الطلب غير مؤكدة، لا يُنصح بالقرار الآلي"
        }

        return RideAnalysis(
            fare = ride.fare,
            totalKm = km,
            commission = commission,
            fuelCost = fuel,
            otherExpenses = others,
            netProfit = net,
            farePerKm = farePerKm,
            netPerKm = netPerKm,
            netPerHour = netPerHour,
            rating = rating,
            reliable = reliable,
            reasons = reasons,
        )
    }

    fun ratingByFarePerKm(farePerKm: Double): Rating = when {
        farePerKm >= FARE_PER_KM_GOOD -> Rating.EXCELLENT
        farePerKm >= FARE_PER_KM_AVERAGE -> Rating.GOOD
        farePerKm >= FARE_PER_KM_WEAK -> Rating.AVERAGE
        else -> Rating.BAD
    }
}
