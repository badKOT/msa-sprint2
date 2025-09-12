package self.project.practicum.s2.dto

import java.time.LocalDate

data class PromoCodeRsDto(
    val code: String,
    val discount: Double,
    val vipOnly: Boolean,
    val expired: Boolean,
    val validUntil: LocalDate,
    val description: String
)