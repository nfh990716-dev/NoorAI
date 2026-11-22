package com.noor.salhi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noor.salhi.engine.CostProfile
import com.noor.salhi.engine.Expense
import com.noor.salhi.engine.Rating
import com.noor.salhi.engine.Ride
import com.noor.salhi.engine.RideEvaluator
import java.util.Locale

private val Ink = Color(0xFF081416)
private val Panel = Color(0xFF122326)
private val Mint = Color(0xFF4DE0B5)
private val Muted = Color(0xFFA9B9B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NoorTheme { NoorHomeScreen() } }
    }
}

@Composable
private fun NoorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Mint,
            onPrimary = Ink,
            background = Ink,
            surface = Panel,
            onBackground = Color.White,
            onSurface = Color.White,
            secondary = Color(0xFF79B8FF)
        ),
        content = content
    )
}

@Composable
private fun NoorHomeScreen() {
    var fare by remember { mutableStateOf("10000") }
    var pickup by remember { mutableStateOf("2") }
    var trip by remember { mutableStateOf("3") }
    var minutes by remember { mutableStateOf("20") }
    var commission by remember { mutableStateOf("20") }
    var fuelPer100 by remember { mutableStateOf("6500") }
    var fuelEnabled by remember { mutableStateOf(true) }
    var maintenance by remember { mutableStateOf(true) }
    var tires by remember { mutableStateOf(true) }
    var depreciation by remember { mutableStateOf(true) }
    var reserve by remember { mutableStateOf(true) }

    val profile = CostProfile(
        name = "مخصص",
        commissionPercent = commission.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 20.0,
        fuelEnabled = fuelEnabled,
        fuelPer100Km = fuelPer100.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 6500.0,
        expenses = listOf(
            Expense("maintenance", "الصيانة", maintenance, perKm = 30.0),
            Expense("tires", "الإطارات", tires, perKm = 20.0),
            Expense("depreciation", "استهلاك السيارة", depreciation, perKm = 60.0),
            Expense("reserve", "الاحتياطي", reserve, perKm = 20.0)
        )
    )
    val fareValue = fare.toDoubleOrNull()
    val pickupValue = pickup.toDoubleOrNull()
    val tripValue = trip.toDoubleOrNull()
    val minutesValue = minutes.toIntOrNull()
    val valid = fareValue != null && pickupValue != null && tripValue != null && minutesValue != null &&
        fareValue >= 0 && pickupValue >= 0 && tripValue >= 0 && minutesValue >= 0 &&
        commission.toDoubleOrNull() != null && fuelPer100.toDoubleOrNull() != null
    val result = if (valid) RideEvaluator.analyze(
        Ride(fareValue!!, pickupValue!!, tripValue!!, minutesValue!!), profile
    ) else null

    Column(
        modifier = Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NOOR AL-SALHI", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("نور الصالحي", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                Text("AI DRIVER INTELLIGENCE", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            Surface(color = Color(0xFF173A35), shape = RoundedCornerShape(50)) {
                Text("نسخة تجريبية", color = Mint, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp)
            }
        }

        SectionCard("حلّل طلبك") {
            NumberField("أجرة الطلب (دينار عراقي)", fare, { fare = it })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { NumberField("مسافة الوصول كم", pickup, { pickup = it }) }
                Box(Modifier.weight(1f)) { NumberField("مسافة الرحلة كم", trip, { trip = it }) }
            }
            NumberField("الوقت بالدقائق", minutes, { minutes = it }, decimal = false)
        }

        SectionCard("التكاليف والإعدادات") {
            NumberField("عمولة الشركة %", commission, { commission = it })
            NumberField("البنزين لكل 100 كم", fuelPer100, { fuelPer100 = it })
            ExpenseToggle("احتساب البنزين", fuelEnabled) { fuelEnabled = it }
            HorizontalDivider(color = Color(0xFF294044))
            ExpenseToggle("الصيانة — 30 د.ع/كم", maintenance) { maintenance = it }
            ExpenseToggle("الإطارات — 20 د.ع/كم", tires) { tires = it }
            ExpenseToggle("استهلاك السيارة — 60 د.ع/كم", depreciation) { depreciation = it }
            ExpenseToggle("احتياطي — 20 د.ع/كم", reserve) { reserve = it }
            Text("القيم قابلة للتعديل في هذه النسخة، وسيتم تطوير إدارة ملفات متعددة لاحقاً.", color = Muted, fontSize = 11.sp)
        }

        if (result != null) {
            SectionCard("نتيجة التحليل") {
                Text(result.rating.label, color = ratingColor(result.rating), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                MetricRow("صافي الربح التقديري", money(result.netProfit), highlight = true)
                MetricRow("المسافة الإجمالية", "${format(result.totalKm)} كم")
                MetricRow("الأجرة لكل كم", money(result.farePerKm))
                MetricRow("صافي الربح لكل كم", money(result.netPerKm))
                MetricRow("العمولة", money(result.commission))
                MetricRow("البنزين", money(result.fuelCost))
                MetricRow("بقية المصاريف", money(result.otherExpenses))
                MetricRow("صافي الربح لكل ساعة", result.netPerHour?.let { money(it) } ?: "غير متاح")
                if (result.reasons.isNotEmpty()) {
                    HorizontalDivider(color = Color(0xFF294044))
                    result.reasons.forEach { Text("• $it", color = Muted, fontSize = 12.sp) }
                }
            }
        } else {
            Text("تحقق من إدخال أرقام صحيحة وغير سالبة.", color = Color(0xFFFFC078))
        }
        Text("الحساب تقديري ويعتمد على البيانات التي تدخلها، وليس عرضاً مؤكداً من شركة التوصيل.", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = Mint, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, decimal: Boolean = true) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> if (input.length <= 12 && input.all { it.isDigit() || (decimal && it == '.') }) onValueChange(input) },
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Mint,
            unfocusedBorderColor = Color(0xFF395154),
            focusedLabelColor = Mint,
            cursorColor = Mint
        )
    )
}

@Composable
private fun ExpenseToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun MetricRow(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 12.sp)
        Text(value, color = if (highlight) Mint else Color.White, fontSize = if (highlight) 17.sp else 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun ratingColor(rating: Rating): Color = when (rating) {
    Rating.EXCELLENT, Rating.GOOD -> Mint
    Rating.AVERAGE -> Color(0xFFFFD166)
    Rating.WEAK, Rating.BAD -> Color(0xFFFF7B72)
}

private fun money(value: Double): String = String.format(Locale.US, "%,.0f د.ع", value)
private fun format(value: Double): String = String.format(Locale.US, "%.1f", value)
