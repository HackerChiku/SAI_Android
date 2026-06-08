package com.saicomputer.sms.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saicomputer.sms.core.format.REGISTRATION_SESSION_LABELS
import com.saicomputer.sms.core.format.STUDENT_STATUS_LABELS
import com.saicomputer.sms.core.ui.theme.StatusAmber
import com.saicomputer.sms.core.ui.theme.StatusBlue
import com.saicomputer.sms.core.ui.theme.StatusEmerald
import com.saicomputer.sms.core.ui.theme.StatusGray
import com.saicomputer.sms.core.ui.theme.StatusPurple
import com.saicomputer.sms.core.ui.theme.StatusRed
import com.saicomputer.sms.core.ui.theme.StatusZinc
import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.StudentStatus

private val STUDENT_STATUS_COLORS: Map<StudentStatus, Color> = mapOf(
    StudentStatus.New to StatusBlue,
    StudentStatus.Active to StatusEmerald,
    StudentStatus.PaymentPending to StatusAmber,
    StudentStatus.Completed to StatusGray,
    StudentStatus.Dropout to StatusRed,
    StudentStatus.NotTakenAdmission to StatusZinc
)

@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun StatusBadge(
    status: StudentStatus,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp
) {
    val color = STUDENT_STATUS_COLORS[status] ?: StatusGray
    Pill(
        text = STUDENT_STATUS_LABELS[status] ?: status.name,
        color = color,
        modifier = modifier,
        fontSize = fontSize
    )
}

/** Hidden for NewRecord (no badge), per spec. */
@Composable
fun RegistrationSessionBadge(
    session: RegistrationSession,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 11.sp
) {
    when (session) {
        RegistrationSession.Before2017 ->
            Pill(REGISTRATION_SESSION_LABELS.getValue(session), StatusPurple, modifier, fontSize)
        RegistrationSession.After2017 ->
            Pill(REGISTRATION_SESSION_LABELS.getValue(session), StatusBlue, modifier, fontSize)
        RegistrationSession.NewRecord -> Unit
    }
}

@Composable
fun GenericBadge(text: String, color: Color = StatusGray, modifier: Modifier = Modifier) {
    Pill(text = text, color = color, modifier = modifier)
}
