package org.example.models

import java.math.BigDecimal

data class SummarizerOperations(
    val weightedAveragePrice: BigDecimal,
    val quantity: BigDecimal,
    val tax: List<Tax>,
    val loss: BigDecimal
)