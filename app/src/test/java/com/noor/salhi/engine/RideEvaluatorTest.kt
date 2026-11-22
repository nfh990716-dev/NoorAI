package com.noor.salhi.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RideEvaluatorTest {

    private val fuelOnly = CostProfile(
        name = "اقتصادي",
        expenses = emptyList(),
    )

    @Test
    fun commissionAndFuelAreComputed() {
        // أجرة 10000، عمولة 20% = 2000، المسافة 5 كم، بنزين 65 د/كم = 325
        val ride = Ride(fare = 10000.0, pickupKm = 2.0, tripKm = 3.0, minutes = 20)
        val r = RideEvaluator.analyze(ride, fuelOnly)
        assertEquals(2000.0, r.commission, 0.001)
        assertEquals(325.0, r.fuelCost, 0.001)
        assertEquals(7675.0, r.netProfit, 0.001)
    }

    @Test
    fun disabledFuelIsNotCounted() {
        val profile = fuelOnly.copy(fuelEnabled = false)
        val r = RideEvaluator.analyze(Ride(10000.0, 2.0, 3.0, 20), profile)
        assertEquals(0.0, r.fuelCost, 0.001)
        assertEquals(8000.0, r.netProfit, 0.001)
    }

    @Test
    fun defaultExpensesAreSubtractedPerKm() {
        // 5 كم: صيانة 30 + إطارات 20 + استهلاك 60 + احتياطي 20 = 130 لكل كم = 650
        val profile = CostProfile(name = "كامل", fuelEnabled = false)
        val r = RideEvaluator.analyze(Ride(10000.0, 2.0, 3.0, 20), profile)
        assertEquals(650.0, r.otherExpenses, 0.001)
        assertEquals(8000.0 - 650.0, r.netProfit, 0.001)
    }

    @Test
    fun singleExpenseCanBeTurnedOff() {
        val profile = CostProfile(
            name = "مخصص",
            fuelEnabled = false,
            expenses = CostProfile.defaultExpenses().map {
                if (it.id == "maintenance") it.copy(enabled = false) else it
            },
        )
        val r = RideEvaluator.analyze(Ride(10000.0, 2.0, 3.0, 20), profile)
        assertEquals(650.0 - 150.0, r.otherExpenses, 0.001)
    }

    @Test
    fun ratingFollowsFarePerKmThresholds() {
        assertEquals(Rating.BAD, RideEvaluator.ratingByFarePerKm(249.9))
        assertEquals(Rating.AVERAGE, RideEvaluator.ratingByFarePerKm(250.0))
        assertEquals(Rating.GOOD, RideEvaluator.ratingByFarePerKm(300.0))
        assertEquals(Rating.EXCELLENT, RideEvaluator.ratingByFarePerKm(350.0))
    }

    @Test
    fun highFareButLowNetIsNotExcellent() {
        // أجرة ممتازة لكل كم، لكن المصاريف تأكل الربح
        val profile = CostProfile(
            name = "مرتفع التكاليف",
            commissionPercent = 20.0,
            fuelPer100Km = 0.0,
            expenses = listOf(Expense("x", "مصروف", perKm = 300.0)),
            minNetProfit = 1000.0,
        )
        val ride = Ride(fare = 3500.0, pickupKm = 0.0, tripKm = 10.0, minutes = 30)
        val r = RideEvaluator.analyze(ride, profile)
        assertEquals(Rating.EXCELLENT, RideEvaluator.ratingByFarePerKm(r.farePerKm))
        assertTrue(r.rating.ordinal <= Rating.WEAK.ordinal)
    }

    @Test
    fun losingRideIsBad() {
        val profile = CostProfile(name = "خاسر", fuelPer100Km = 0.0,
            expenses = listOf(Expense("x", "مصروف", perKm = 500.0)))
        val r = RideEvaluator.analyze(Ride(1000.0, 1.0, 1.0, 10), profile)
        assertEquals(Rating.BAD, r.rating)
    }

    @Test
    fun lowConfidenceMarksRideUnreliable() {
        val r = RideEvaluator.analyze(Ride(10000.0, 2.0, 3.0, 20, confidence = 0.3), fuelOnly)
        assertFalse(r.reliable)
    }

    @Test
    fun zeroDistanceDoesNotCrash() {
        val r = RideEvaluator.analyze(Ride(5000.0, 0.0, 0.0, 10), fuelOnly)
        assertEquals(0.0, r.farePerKm, 0.001)
        assertFalse(r.reliable)
    }

    @Test
    fun hourlyIsNullWhenTimeUnknown() {
        val r = RideEvaluator.analyze(Ride(10000.0, 2.0, 3.0, 0), fuelOnly)
        assertNull(r.netPerHour)
    }
}
