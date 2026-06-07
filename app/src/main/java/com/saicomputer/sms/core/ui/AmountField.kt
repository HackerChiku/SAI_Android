package com.saicomputer.sms.core.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Digits-only whole-rupee amount input. Emits the parsed Int (0 when empty).
 */
@Composable
fun AmountField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = if (value == 0) "" else value.toString(),
        onValueChange = { raw ->
            val digits = raw.filter { it.isDigit() }.take(9)
            onValueChange(digits.toIntOrNull() ?: 0)
        },
        label = { Text(label) },
        prefix = { Text("₹ ") },
        singleLine = true,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}
