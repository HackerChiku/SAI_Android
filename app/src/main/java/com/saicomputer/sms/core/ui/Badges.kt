package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.format.STUDENT_STATUS_LABELS
import com.saicomputer.sms.core.ui.theme.appColors
import com.saicomputer.sms.core.ui.theme.appDimens
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.StudentStatus

@Composable
fun studentStatusColor(status: StudentStatus): Color {
    val colors = appColors()
    return when (status) {
        StudentStatus.New -> colors.info
        StudentStatus.Active -> colors.success
        StudentStatus.PaymentPending -> colors.warning
        StudentStatus.Completed -> colors.neutral
        StudentStatus.Dropout -> colors.error
        StudentStatus.NotTakenAdmission -> colors.neutralMuted
    }
}

@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall
) {
    val dimens = appDimens()
    Text(
        text = text,
        color = color,
        style = style,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(dimens.pillShape)
            .background(color.copy(alpha = 0.25f))
            .padding(horizontal = dimens.badgePaddingH, vertical = dimens.badgePaddingV)
    )
}

@Composable
fun StatusBadge(
    status: StudentStatus,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall
) {
    val color = studentStatusColor(status)
    Pill(
        text = STUDENT_STATUS_LABELS[status] ?: status.name,
        color = color,
        modifier = modifier,
        style = style
    )
}

/** Hidden for NewRecord (no badge), per spec. */
@Composable
fun RegistrationSessionBadge(
    session: RegistrationSession,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelSmall
) {
    val colors = appColors()
    when (session) {
        RegistrationSession.Before2017 ->
            Pill(REGISTRATION_SESSION_LABELS.getValue(session), colors.purple, modifier, style)
        RegistrationSession.After2017 ->
            Pill(REGISTRATION_SESSION_LABELS.getValue(session), colors.info, modifier, style)
        RegistrationSession.NewRecord -> Unit
    }
}

@Composable
fun GenericBadge(text: String, color: Color? = null, modifier: Modifier = Modifier) {
    Pill(text = text, color = color ?: appColors().neutral, modifier = modifier)
}
