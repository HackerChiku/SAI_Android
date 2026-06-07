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

@Composable
fun MonthlyRevenueChart(
    points: List<MonthlyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
            }
        },
        update = { chart ->
            val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.amount.toFloat()) }
            val set = LineDataSet(entries, "Revenue").apply {
                color = AndroidColor.parseColor("#1E3A8A")
                setCircleColor(AndroidColor.parseColor("#1E3A8A"))
                lineWidth = 2f
                circleRadius = 3f
                setDrawValues(false)
                setDrawFilled(true)
                fillColor = AndroidColor.parseColor("#1E3A8A")
                fillAlpha = 40
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
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 50f
                transparentCircleRadius = 54f
                setUsePercentValues(true)
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }
        },
        update = { chart ->
            val entries = buildList {
                if (breakdown.UPI > 0) add(PieEntry(breakdown.UPI.toFloat(), "UPI"))
                if (breakdown.CASH > 0) add(PieEntry(breakdown.CASH.toFloat(), "CASH"))
                if (breakdown.QR > 0) add(PieEntry(breakdown.QR.toFloat(), "QR"))
            }
            val set = PieDataSet(entries, "").apply {
                colors = listOf(
                    AndroidColor.parseColor("#2563EB"),
                    AndroidColor.parseColor("#059669"),
                    AndroidColor.parseColor("#D97706")
                )
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 12f
                sliceSpace = 2f
            }
            chart.data = PieData(set)
            chart.invalidate()
        }
    )
}

private fun shortMonth(month: String): String {
    // month is "yyyy-MM"; show "MMM"
    return try {
        val m = month.split("-")[1].toInt()
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[m - 1]
    } catch (e: Exception) {
        month
    }
}
