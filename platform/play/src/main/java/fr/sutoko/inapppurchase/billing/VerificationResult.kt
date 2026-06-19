package fr.sutoko.inapppurchase.billing

import androidx.annotation.Keep

@Keep
data class VerificationResult(
    val verified: Boolean,
    val message: String? = null,
)
