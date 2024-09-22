package org.example.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class Operation (
    val operation: String,
    @JsonProperty("unit-cost")
    val unitCost: BigDecimal,
    val quantity: BigDecimal
)
