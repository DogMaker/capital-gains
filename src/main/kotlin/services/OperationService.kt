package org.example.services

import org.example.calculations.*
import org.example.helpers.setScaleDown
import org.example.models.Operation
import org.example.models.Summarizer
import org.example.models.Tax
import java.math.BigDecimal.ZERO


fun calculateTax(operations: List<Operation>): List<Tax> {

    val resp = operations.fold(
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
                    tax = summarizer.tax + Tax(tax = ZERO.setScaleDown())
                )
            }
            "sell" -> {
                val currentQuantityStocks = summarizer.quantity - operation.quantity
                val totalSellOperation = operation.unitCost * operation.quantity

                val gains = calculateGains(operation.unitCost, summarizer.weightedAveragePrice, operation.quantity)
                val loss = accumulatedLoss(operation, summarizer)

                val tax = when{
                    isThereLoss(operation.unitCost, summarizer.weightedAveragePrice) -> ZERO
                    isTaxable(totalSellOperation) -> calculateTax(summarizer, gains)
                    else -> ZERO
                }

                summarizer.copy(
                    quantity = currentQuantityStocks,
                    tax = summarizer.tax + Tax(tax = tax.setScaleDown()),
                    loss =  loss
                )
            }
            else -> summarizer
        }
    }

    return resp.tax
}
