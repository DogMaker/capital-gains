package org.example

import org.example.models.Operation
import org.example.models.SummarizerOperations
import org.example.models.Tax
import java.math.BigDecimal.ZERO
import java.math.BigDecimal.ONE
import java.math.BigDecimal
import java.math.RoundingMode

val isProfitSufficientForTax: (BigDecimal) -> Boolean = { operationValue ->
    operationValue > BigDecimal(20000.00)
}

val computeGains: (BigDecimal, BigDecimal, BigDecimal) -> BigDecimal = {
        currentUnitCosts: BigDecimal,
        weightedAverage: BigDecimal,
        quantitySold: BigDecimal
    -> (currentUnitCosts - weightedAverage) * (quantitySold.takeIf { it > ZERO }?: ONE)
}
val isThereLoss: (BigDecimal, BigDecimal) -> Boolean = { unitCost, weightedAverage ->
    unitCost < weightedAverage
}

val computeLost: (BigDecimal, BigDecimal, BigDecimal) -> BigDecimal = { unitCost, weightedAverage, quantitySold ->
    (unitCost - weightedAverage) * quantitySold
}
val setTax: (BigDecimal) -> BigDecimal = { result -> (result * BigDecimal(0.2)).setScale(2, RoundingMode.DOWN) }

fun process1(operations: List<Operation>): List<Tax> {

    val resp = operations.fold(
        SummarizerOperations(
            ZERO,
            ZERO,
            emptyList(),
            ZERO
        )
    ) {
      summarizer, operation ->
        when (operation.operation) {
            "buy" -> {
                val currentQuantityStocks = summarizer.quantity + operation.quantity
                val weightedAveragePrice = summarizer.weightedAveragePrice.takeIf { it != ZERO } ?: operation.unitCost

                val totalValueOfCurrentStocks = summarizer.quantity * weightedAveragePrice
                val totalValueOfPurchasedStocks = operation.quantity * operation.unitCost

                val newWeightedAveragePrice = (totalValueOfCurrentStocks + totalValueOfPurchasedStocks) / currentQuantityStocks

                summarizer.copy(
                    weightedAveragePrice = newWeightedAveragePrice,
                    quantity = currentQuantityStocks,
                    tax = summarizer.tax + Tax(tax = setTax(BigDecimal(0.00)))
                )
            }
            "sell" -> {
                val currentQuantityStocks = summarizer.quantity - operation.quantity

                val gains = computeGains(operation.unitCost, summarizer.weightedAveragePrice, operation.quantity)

                val isThereLoss= isThereLoss(operation.unitCost, summarizer.weightedAveragePrice)
                val totalOperationSell = operation.unitCost * operation.quantity

                val tax = if(isThereLoss){
                    setTax(BigDecimal(0.00))
                }else if (isProfitSufficientForTax(totalOperationSell)){
                    setTax(gains)
                }else{
                    setTax(BigDecimal(0.00))
                }

                summarizer.copy(
                    quantity = currentQuantityStocks,
                    tax = summarizer.tax + Tax(tax = tax),
                    loss = computeLost(operation.unitCost, summarizer.weightedAveragePrice, operation.quantity)
                )
            }
            else -> summarizer
        }.also { println(it) }
    }

    return resp.tax
}


val calculate: (List<Operation>) -> List<Tax> = { operations ->
    process1(operations)
}