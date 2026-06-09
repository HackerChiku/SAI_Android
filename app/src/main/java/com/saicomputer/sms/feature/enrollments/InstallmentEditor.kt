package com.saicomputer.sms.feature.enrollments

import com.saicomputer.sms.core.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.core.format.Formatters
import com.saicomputer.sms.core.ui.AmountField
import com.saicomputer.sms.core.ui.DatePickerField
import com.saicomputer.sms.core.ui.GenericBadge
import com.saicomputer.sms.core.validation.InstallmentDraft
import com.saicomputer.sms.core.validation.InstallmentValidation
import com.saicomputer.sms.core.ui.theme.appDimens

data class EditableInstallment(
    val installmentId: String? = null,
    val amountDue: Int,
    val dueDate: String,
    val amountPaid: Int = 0
)

/**
 * Editable installment list. Paid rows (amountPaid > 0) are tinted, floored at
 * amountPaid, and cannot be removed. Add is disabled at [ceiling].
 */
@Composable
fun InstallmentEditor(
    rows: List<EditableInstallment>,
    totalRequired: Int,
    ceiling: Int,
    startDate: String?,
    editable: Boolean,
    onRowsChange: (List<EditableInstallment>) -> Unit,
    modifier: Modifier = Modifier
) {
    val drafts = rows.map { InstallmentDraft(it.installmentId, it.amountDue, it.dueDate, it.amountPaid) }
    val result = InstallmentValidation.validate(drafts, totalRequired, ceiling, startDate)
    val sum = result.sum

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(appDimens().spacingSm)) {
        rows.forEachIndexed { index, row ->
            val isPaid = row.amountPaid > 0
            val belowFloor = row.amountDue < row.amountPaid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(appDimens().fieldShape)
                    .background(
                        if (isPaid) appColors().success.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    )
            ) {
                Column(modifier = Modifier.padding(appDimens().spacingMd)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("#${index + 1}", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(appDimens().iconSizeXxl))
                        AmountField(
                            value = row.amountDue,
                            onValueChange = { v ->
                                onRowsChange(rows.toMutableList().also { it[index] = row.copy(amountDue = v) })
                            },
                            label = "Amount",
                            enabled = editable,
                            isError = belowFloor,
                            supportingText = if (isPaid) "min ${Formatters.formatInr(row.amountPaid)} (paid)" else null,
                            modifier = Modifier.weight(1f)
                        )
                        if (editable) {
                            IconButton(
                                onClick = {
                                    if (!isPaid) onRowsChange(rows.toMutableList().also { it.removeAt(index) })
                                },
                                enabled = !isPaid
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                    DatePickerField(
                        value = row.dueDate,
                        onValueChange = { d ->
                            onRowsChange(rows.toMutableList().also { it[index] = row.copy(dueDate = d) })
                        },
                        label = "Due date",
                        enabled = editable
                    )
                    if (isPaid) GenericBadge("Paid ${Formatters.formatInr(row.amountPaid)}", appColors().success)
                }
            }
        }

        if (editable) {
            OutlinedButton(
                onClick = {
                    val lastDate = rows.lastOrNull()?.dueDate ?: (startDate ?: "")
                    onRowsChange(rows + EditableInstallment(amountDue = 0, dueDate = lastDate))
                },
                enabled = rows.size < ceiling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text(if (rows.size < ceiling) "Add installment" else "Maximum reached ($ceiling)")
            }
        }

        val matches = sum == totalRequired
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Total ${Formatters.formatInr(sum)} / Required ${Formatters.formatInr(totalRequired)}",
                color = if (matches) appColors().success else appColors().error,
                fontWeight = FontWeight.SemiBold
            )
            if (!matches) {
                val diff = totalRequired - sum
                Text(
                    if (diff > 0) "(short by ${Formatters.formatInr(diff)})" else "(over by ${Formatters.formatInr(-diff)})",
                    color = appColors().error
                )
            }
        }
        result.errors.forEach { err ->
            Text("• $err", color = appColors().error, style = MaterialTheme.typography.labelSmall)
        }
    }
}

fun installmentsValid(
    rows: List<EditableInstallment>,
    totalRequired: Int,
    ceiling: Int,
    startDate: String?
): Boolean {
    val drafts = rows.map { InstallmentDraft(it.installmentId, it.amountDue, it.dueDate, it.amountPaid) }
    return InstallmentValidation.validate(drafts, totalRequired, ceiling, startDate).isValid
}
