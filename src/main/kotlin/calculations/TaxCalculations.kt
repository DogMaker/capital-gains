package org.example.calculations

import org.example.helpers.LIMIT_AMOUNT_FOR_TAX
import org.example.helpers.PROFIT_TAX
import org.example.models.Operation
import org.example.models.Summarizer
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO

val isTaxable: (BigDecimal) -> Boolean = { operationValue ->
    operationValue > LIMIT_AMOUNT_FOR_TAX
}

val calculateGains: (BigDecimal, BigDecimal, BigDecimal) -> BigDecimal = {
    currentUnitCosts: BigDecimal,
    weightedAverage: BigDecimal,
    quantitySold: BigDecimal
    -> ((currentUnitCosts - weightedAverage) * (quantitySold.takeIf { it > ZERO }?: ONE)).let {
        result -> result.takeIf { it > ZERO } ?: ZERO
    }
}

val accumulatedLoss: (Operation, Summarizer) -> BigDecimal = { operation, summarizer ->
    ((operation.unitCost - summarizer.weightedAveragePrice) * operation.quantity).minus(summarizer.loss).let {
            result -> result.takeIf { it < ZERO } ?: ZERO
    }.abs()
}

val isThereLoss: (BigDecimal, BigDecimal) -> Boolean = { unitCost, weightedAverage ->
    unitCost < weightedAverage
}

fun calculateWeightedAveragePrice(
    summarizer: Summarizer,
    operation: Operation,
    currentQuantityStocks: BigDecimal
): BigDecimal {

    val weightedAveragePrice = summarizer.weightedAveragePrice.takeIf { it != ZERO } ?: operation.unitCost

    val totalCurrentStockValue = summarizer.quantity * weightedAveragePrice
    val totalPurchasedStockValue = operation.quantity * operation.unitCost

    return (totalCurrentStockValue + totalPurchasedStockValue) / currentQuantityStocks
}

fun calculateTax(
    summarizer: Summarizer,
    operation: Operation
): BigDecimal {

    val totalSellOperation = operation.unitCost * operation.quantity
    val gains = calculateGains(operation.unitCost, summarizer.weightedAveragePrice, operation.quantity)

    return when{
        isThereLoss(operation.unitCost, summarizer.weightedAveragePrice) -> BigDecimal(0.00)
        isTaxable(totalSellOperation) -> gains * PROFIT_TAX
        else -> BigDecimal(0.00)
    }
}

fun calculateTax(summarizer: Summarizer, gains: BigDecimal): BigDecimal {
    return when {
        summarizer.loss == ZERO -> gains * PROFIT_TAX
        gains > summarizer.loss -> (gains - summarizer.loss) * PROFIT_TAX
        else -> ZERO
    }
}
