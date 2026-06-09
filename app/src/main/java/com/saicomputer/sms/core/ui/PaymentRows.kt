package com.saicomputer.sms.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun PaymentActionButtons(
    showReceipt: Boolean,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
    showVoid: Boolean = false,
    onVoid: (() -> Unit)? = null,
    showEditMonth: Boolean = false,
    onEditBillingMonth: (() -> Unit)? = null
) {
    val dimens = appDimens()
    val compactPadding = PaddingValues(
        horizontal = dimens.spacingMd,
        vertical = dimens.spacingXs
    )
    val buttonModifier = Modifier.defaultMinSize(minHeight = dimens.spacing32)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showReceipt) {
            OutlinedButton(
                onClick = onViewReceipt,
                modifier = buttonModifier,
                contentPadding = compactPadding,
                shape = dimens.fieldShape
            ) {
                Text("View Receipt", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (showEditMonth && onEditBillingMonth != null) {
            OutlinedButton(
                onClick = onEditBillingMonth,
                modifier = buttonModifier,
                contentPadding = compactPadding,
                shape = dimens.fieldShape
            ) {
                Text("Edit Month", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (showVoid && onVoid != null) {
            OutlinedButton(
                onClick = onVoid,
                modifier = buttonModifier,
                contentPadding = compactPadding,
                shape = dimens.fieldShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                ),
                border = BorderStroke(dimens.strokeHairline, MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Void", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun ReceiptActionButtons(
    onView: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = appDimens()
    val buttonPadding = PaddingValues(
        horizontal = dimens.spacingLg,
        vertical = dimens.spacingSm
    )
    val buttonModifier = Modifier.defaultMinSize(
        minHeight = dimens.callButtonSize,
        minWidth = dimens.callButtonSize
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onView,
            modifier = buttonModifier,
            contentPadding = buttonPadding,
            shape = dimens.fieldShape
        ) {
            Text("View", style = MaterialTheme.typography.labelLarge)
        }
        OutlinedButton(
            onClick = onResend,
            modifier = buttonModifier,
            contentPadding = buttonPadding,
            shape = dimens.fieldShape
        ) {
            Text("Resend", style = MaterialTheme.typography.labelLarge)
        }
    }
}
