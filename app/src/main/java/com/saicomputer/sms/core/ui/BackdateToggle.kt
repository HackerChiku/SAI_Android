package com.saicomputer.sms.core.ui

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.core.ui.theme.appDimens

/**
 * Owner-only backdate control with amber styling. When [enabled], shows a
 * date picker (future disabled) and warns when the date is far in the past.
 */
@Composable
fun BackdateToggle(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    date: String?,
    onDateChange: (String) -> Unit,
    dateLabel: String = "Effective date",
    explanation: String = "Backdating records the entry as of a past date. No email is auto-sent.",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(appDimens().strokeHairline, appColors().warning.copy(alpha = 0.6f), appDimens().fieldShape)
            .background(appColors().warning.copy(alpha = 0.06f), appDimens().fieldShape)
            .padding(appDimens().spacingMd)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Backdate this entry", fontWeight = FontWeight.SemiBold, color = appColors().warning)
                Text(
                    explanation,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        if (enabled) {
            Spacer(Modifier.height(appDimens().spacingSm))
            DatePickerField(
                value = date,
                onValueChange = onDateChange,
                label = dateLabel,
                disableFuture = true
            )
        }
    }
}
