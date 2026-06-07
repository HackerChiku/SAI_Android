package com.saicomputer.sms.feature.enrollments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.core.ui.theme.StatusEmerald

@Composable
fun EnrollmentFeeWaiverBanner(waivedFromCourseName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusEmerald.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text("Enrollment fee waived", fontWeight = FontWeight.SemiBold, color = StatusEmerald)
        Text(
            "Already paid via \"$waivedFromCourseName\". Defaulted to ₹0 (you can override).",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
