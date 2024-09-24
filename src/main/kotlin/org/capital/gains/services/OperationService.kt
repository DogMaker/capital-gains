package org.capital.gains.services

import org.capital.gains.calculations.accumulatedLoss
import org.capital.gains.calculations.calculateSellTax
import org.capital.gains.calculations.calculateWeightedAveragePrice
import org.capital.gains.helpers.setScaleHalf
import org.capital.gains.models.Operation
import org.capital.gains.models.Summarizer
import org.capital.gains.models.Tax
import java.math.BigDecimal.ZERO


fun calculateTax(operations: List<Operation>): List<Tax> {

    val calculations = operations.fold(
        Summarizer(
            weightedAveragePrice = ZERO,
            quantity = ZERO,
            tax = emptyList(),
            loss = ZERO
        )
    ) { summarizer, operation ->

        when (operation.operation) {
            "buy" -> {
                val currentQuantityStocks = summarizer.quantity + operation.quantity

                val newWeightedAveragePrice = calculateWeightedAveragePrice(
                    summarizer,
                    operation,
                    currentQuantityStocks
                )

                summarizer.copy(
                    weightedAveragePrice = newWeightedAveragePrice,
                    quantity = currentQuantityStocks,
                    tax = summarizer.tax + Tax(tax = ZERO.setScaleHalf())
                )
            }

            "sell" -> {
                val currentQuantityStocks = summarizer.quantity - operation.quantity
                val loss = accumulatedLoss(operation, summarizer)

                summarizer.copy(
                    quantity = currentQuantityStocks,
                    tax = summarizer.tax + Tax(tax = calculateSellTax(summarizer, operation).setScaleHalf()),
                    loss = loss
                )
            }

            else -> summarizer
        }
    }

    return calculations.tax
}
