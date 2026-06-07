package com.saicomputer.sms.core.ui

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.saicomputer.sms.core.format.Formatters

@Composable
fun CurrencyText(
    amount: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    bold: Boolean = false
) {
    Text(
        text = Formatters.formatInr(amount),
        modifier = modifier,
        color = color,
        style = style,
        fontWeight = if (bold) FontWeight.SemiBold else null
    )
}

@Composable
fun DateText(
    iso: String?,
    withTime: Boolean = false,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = Formatters.formatDateIst(iso, withTime),
        modifier = modifier,
        color = color,
        style = style
    )
}
