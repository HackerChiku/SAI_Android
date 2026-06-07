package com.saicomputer.sms.core.validation

/**
 * A draft installment row used by the editor / wizard / edit dialog.
 * [amountPaid] is read-only context (the paid floor) and 0 for new rows.
 */
data class InstallmentDraft(
    val installmentId: String? = null,
    val amountDue: Int,
    val dueDate: String, // yyyy-MM-dd
    val amountPaid: Int = 0
)

data class InstallmentValidationResult(
    val errors: List<String>,
    val sum: Int,
    val isValid: Boolean
) {
    val difference: Int get() = 0
}

object InstallmentValidation {

    /**
     * Validates a set of installment rows.
     *
     * Rules (consolidated v4 + v11):
     *  - non-empty; count <= maxCount; sum == totalRequired;
     *  - each amount > 0 (whole rupees); due dates strictly increasing;
     *  - first due date >= startDate (creation only, when [startDate] given);
     *  - post-payment: each row's amountDue >= its amountPaid (paid floor).
     */
    fun validate(
        rows: List<InstallmentDraft>,
        totalRequired: Int,
        maxCount: Int,
        startDate: String? = null
    ): InstallmentValidationResult {
        val errors = mutableListOf<String>()

        if (rows.isEmpty()) {
            errors += "Add at least one installment"
        }
        if (rows.size > maxCount) {
            errors += "Too many installments (max $maxCount)"
        }

        rows.forEachIndexed { index, row ->
            val n = index + 1
            if (row.amountDue <= 0) {
                errors += "Installment $n amount must be greater than 0"
            }
            if (row.amountDue < row.amountPaid) {
                errors += "Installment $n cannot be below paid amount (₹${row.amountPaid})"
            }
        }

        // strictly increasing due dates
        for (i in 1 until rows.size) {
            if (rows[i].dueDate <= rows[i - 1].dueDate) {
                errors += "Due dates must be in increasing order"
                break
            }
        }

        if (startDate != null && rows.isNotEmpty() && rows.first().dueDate < startDate) {
            errors += "First due date cannot be before the start date"
        }

        val sum = rows.sumOf { it.amountDue }
        if (sum != totalRequired) {
            val diff = totalRequired - sum
            if (diff > 0) {
                errors += "Total is short by ₹$diff (need ₹$totalRequired)"
            } else {
                errors += "Total exceeds required by ₹${-diff} (need ₹$totalRequired)"
            }
        }

        return InstallmentValidationResult(
            errors = errors,
            sum = sum,
            isValid = errors.isEmpty()
        )
    }
}
