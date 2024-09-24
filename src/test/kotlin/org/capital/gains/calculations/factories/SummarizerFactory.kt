package org.capital.gains.calculations.factories

import org.capital.gains.models.Summarizer
import java.math.BigDecimal

val summarizer = Summarizer(
    weightedAveragePrice = BigDecimal("10.00"),
    quantity= BigDecimal("2"),
    tax= listOf(),
    loss= BigDecimal("20.00")
)