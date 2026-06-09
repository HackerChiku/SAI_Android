package com.saicomputer.sms.feature.enrollments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saicomputer.sms.data.dto.InstallmentEditRow
import com.saicomputer.sms.data.model.Installment
import com.saicomputer.sms.core.ui.theme.appDimens

@Composable
fun InstallmentEditDialog(
    installments: List<Installment>,
    totalRequired: Int,
    ceiling: Int,
    startDate: String?,
    onSave: (List<InstallmentEditRow>) -> Unit,
    onDismiss: () -> Unit
) {
    var rows by remember {
        mutableStateOf(
            installments.map {
                EditableInstallment(
                    installmentId = it.installmentId,
                    amountDue = it.amountDue,
                    dueDate = it.dueDate,
                    amountPaid = it.amountPaid
                )
            }
        )
    }
    val valid = installmentsValid(rows, totalRequired, ceiling, startDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Installments") },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = appDimens().dialogMaxHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                InstallmentEditor(
                    rows = rows,
                    totalRequired = totalRequired,
                    ceiling = ceiling,
                    startDate = startDate,
                    editable = true,
                    onRowsChange = { rows = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    onSave(rows.map { InstallmentEditRow(it.installmentId, it.amountDue, it.dueDate) })
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
