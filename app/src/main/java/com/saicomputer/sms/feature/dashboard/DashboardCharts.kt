package com.saicomputer.sms.feature.dashboard

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.saicomputer.sms.core.ui.theme.appColors
import com.saicomputer.sms.core.ui.theme.toArgbInt
import com.saicomputer.sms.data.model.MonthlyRevenuePoint
import com.saicomputer.sms.data.model.PaymentMethodBreakdown
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun MonthlyRevenueChart(
    points: List<MonthlyRevenuePoint>,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val lineColor = colors.chartLine.toArgbInt()
    val gridColor = colors.chartGrid.toArgbInt()
    val axisColor = colors.chartAxis.toArgbInt()
    val xAxisColor = colors.chartAxisX.toArgbInt()
    val pointColor = colors.chartPoint.toArgbInt()
    val fillStart = colors.chartFillStart.toArgbInt()

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(appDimens().chartHeightLine),
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false
                setTouchEnabled(true)
                setScaleEnabled(false)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                axisLeft.setDrawGridLines(true)
                setDrawGridBackground(false)
            }
        },
        update = { chart ->
            chart.xAxis.textColor = xAxisColor
            chart.axisLeft.gridColor = gridColor
            chart.axisLeft.textColor = axisColor

            val entries = points.mapIndexed { i, p -> Entry(i.toFloat(), p.amount.toFloat()) }
            val set = LineDataSet(entries, "Revenue").apply {
                color = lineColor
                setCircleColor(if (colors.isDark) pointColor else lineColor)
                circleHoleColor = lineColor
                circleRadius = if (colors.isDark) 5f else 4f
                circleHoleRadius = if (colors.isDark) 3f else 2f
                lineWidth = if (colors.isDark) 3f else 2.5f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                if (colors.isDark) {
                    setDrawFilled(true)
                    fillDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        intArrayOf(fillStart, AndroidColor.TRANSPARENT)
                    )
                } else {
                    setDrawFilled(false)
                }
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
    val colors = appColors()
    val total = breakdown.UPI + breakdown.CASH + breakdown.QR
    val chartColors = listOf(
        colors.chartUpi.toArgbInt(),
        colors.chartCash.toArgbInt(),
        colors.chartQr.toArgbInt()
    )

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(appDimens().chartHeightPie),
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 58f
                transparentCircleRadius = 62f
                setUsePercentValues(false)
                legend.isEnabled = false
                setDrawEntryLabels(false)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setHoleColor(AndroidColor.TRANSPARENT)
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
                this.colors = chartColors
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
