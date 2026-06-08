package com.saicomputer.sms.feature.dashboard

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.saicomputer.sms.data.model.MonthlyRevenuePoint
import com.saicomputer.sms.data.model.PaymentMethodBreakdown

private const val CHART_BLUE = "#0A3D91"
private const val CHART_RED = "#BE123C"
private const val CHART_GREEN = "#16A34A"

@Composable
fun MonthlyRevenueChart(
    points: List<MonthlyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textColor = AndroidColor.parseColor("#6B7280")
                xAxis.granularity = 1f
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = AndroidColor.parseColor("#E5E7EB")
                axisLeft.textColor = AndroidColor.parseColor("#9CA3AF")
                setDrawGridBackground(false)
            }
        },
        update = { chart ->
            val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.amount.toFloat()) }
            val set = LineDataSet(entries, "Revenue").apply {
                color = AndroidColor.parseColor(CHART_BLUE)
                setCircleColor(AndroidColor.parseColor(CHART_BLUE))
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawValues(false)
                setDrawFilled(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            chart.data = LineData(set)
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(points.map { shortMonth(it.month) })
            chart.invalidate()
        }
    )
}

@Composable
fun PaymentMethodsChart(
    breakdown: PaymentMethodBreakdown,
    modifier: Modifier = Modifier
) {
    val total = breakdown.UPI + breakdown.CASH + breakdown.QR
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 62f
                setUsePercentValues(false)
                legend.isEnabled = false
                setDrawEntryLabels(false)
            }
        },
        update = { chart ->
            val entries = buildList {
                if (breakdown.UPI > 0) add(PieEntry(breakdown.UPI.toFloat(), "UPI"))
                if (breakdown.CASH > 0) add(PieEntry(breakdown.CASH.toFloat(), "Cash"))
                if (breakdown.QR > 0) add(PieEntry(breakdown.QR.toFloat(), "QR"))
            }
            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }
            val set = PieDataSet(entries, "").apply {
                colors = listOf(
                    AndroidColor.parseColor(CHART_BLUE),
                    AndroidColor.parseColor(CHART_RED),
                    AndroidColor.parseColor(CHART_GREEN)
                )
                sliceSpace = 3f
                setDrawValues(false)
            }
            chart.data = PieData(set)
            chart.centerText = if (total > 0) "" else "No data"
            chart.invalidate()
        }
    )
}

fun paymentMethodPercent(value: Int, total: Int): Int =
    if (total > 0) ((value.toFloat() / total) * 100).toInt() else 0

private fun shortMonth(month: String): String {
    return try {
        val m = month.split("-")[1].toInt()
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[m - 1]
    } catch (e: Exception) {
        month
    }
}
