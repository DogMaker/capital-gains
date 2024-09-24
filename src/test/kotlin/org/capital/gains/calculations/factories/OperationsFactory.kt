package org.capital.gains.calculations.factories

import org.capital.gains.models.Operation
import java.math.BigDecimal

val operation = Operation(
    operation = "sell",
    unitCost = BigDecimal("20.00"),
    quantity = BigDecimal("2")
)