package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.core.ui.theme.appColors
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
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun EnrollmentFeeWaiverBanner(waivedFromCourseName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(appDimens().fieldShape)
            .background(appColors().success.copy(alpha = 0.1f))
            .padding(appDimens().spacingMd)
    ) {
        Text("Enrollment fee waived", fontWeight = FontWeight.SemiBold, color = appColors().success)
        Text(
            "Already paid via \"$waivedFromCourseName\". Defaulted to ₹0 (you can override).",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
