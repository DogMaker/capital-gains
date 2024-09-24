package org.capital.gains.models

import java.math.BigDecimal

data class Summarizer(
    val weightedAveragePrice: BigDecimal,
    val quantity: BigDecimal,
    val tax: List<Tax>,
    val loss: BigDecimal
)