package com.saicomputer.sms.core.format

import com.saicomputer.sms.data.model.RegistrationSession
import com.saicomputer.sms.data.model.StudentStatus
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {

    private val INDIA: Locale = Locale("en", "IN")
    val IST: ZoneId = ZoneId.of("Asia/Kolkata")

    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(INDIA).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val dateFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", INDIA)
    private val dateTimeFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", INDIA)
    private val monthYearFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", INDIA)

    /** Indian-grouped rupees, no decimals, e.g. ₹1,00,000. */
    fun formatInr(amount: Int): String = inrFormat.format(amount.toLong())

    fun formatInr(amount: Long): String = inrFormat.format(amount)

    /**
     * Render an ISO date/datetime in IST. Accepts `yyyy-MM-dd` (date only),
     * full ISO instants/offset-datetimes. Returns the input on parse failure.
     */
    fun formatDateIst(iso: String?, withTime: Boolean = false): String {
        if (iso.isNullOrBlank()) return ""
        val zoned: ZonedDateTime = parseToZoned(iso) ?: return iso
        return zoned.format(if (withTime) dateTimeFmt else dateFmt)
    }

    /** Format a `yyyy-MM` billing month as "April 2026". */
    fun formatBillingMonth(yyyyMm: String?): String {
        if (yyyyMm.isNullOrBlank()) return ""
        return try {
            val parts = yyyyMm.split("-")
            val date = LocalDate.of(parts[0].toInt(), parts[1].toInt(), 1)
            date.format(monthYearFmt)
        } catch (e: Exception) {
            yyyyMm
        }
    }

    private fun parseToZoned(iso: String): ZonedDateTime? = try {
        when {
            // date only: yyyy-MM-dd
            iso.length == 10 && iso[4] == '-' ->
                LocalDate.parse(iso).atStartOfDay(IST)
            else -> {
                try {
                    Instant.parse(iso).atZone(IST)
                } catch (e: Exception) {
                    OffsetDateTime.parse(iso).atZoneSameInstant(IST)
                }
            }
        }
    } catch (e: Exception) {
        null
    }

    /** 12 digits -> "XXXX-XXXX-1234". Anything else passes through / blanks. */
    fun maskAadhaar(aadhaar: String?): String {
        if (aadhaar.isNullOrBlank()) return ""
        val digits = aadhaar.filter { it.isDigit() }
        if (digits.length != 12) return aadhaar
        return "XXXX-XXXX-${digits.takeLast(4)}"
    }

    /** 10 digits -> "98765 43210". */
    fun formatPhone(phone: String?): String {
        if (phone.isNullOrBlank()) return ""
        val digits = phone.filter { it.isDigit() }
        if (digits.length != 10) return phone
        return "${digits.substring(0, 5)} ${digits.substring(5)}"
    }
}

val STUDENT_STATUS_LABELS: Map<StudentStatus, String> = mapOf(
    StudentStatus.New to "New",
    StudentStatus.Active to "Active",
    StudentStatus.PaymentPending to "Payment Pending",
    StudentStatus.Completed to "Completed",
    StudentStatus.Dropout to "Dropout",
    StudentStatus.NotTakenAdmission to "Not Taken Admission"
)

val REGISTRATION_SESSION_LABELS: Map<RegistrationSession, String> = mapOf(
    RegistrationSession.Before2017 to "Before 2017",
    RegistrationSession.After2017 to "After 2017",
    RegistrationSession.NewRecord to "New Record"
)
