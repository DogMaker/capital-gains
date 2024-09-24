package org.capital.gains.calculations

import org.capital.gains.calculations.factories.operation
import org.capital.gains.calculations.factories.summarizer
import org.capital.gains.helpers.setScaleHalf
import org.capital.gains.models.Operation
import org.capital.gains.models.Summarizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaxCalculationsTests {

    @Test
    fun `when the profit's value is bigger than limit should be taxable`() {
        assertTrue { isTaxable(BigDecimal("2000000.00")) }
    }

    @Test
    fun `when the profit's value is less than limit should be taxable`() {
        assertFalse { isTaxable(BigDecimal("200.00")) }
    }

    @Test
    fun `when the operation does not result on gains or loss should result on ZERO `() {
        val result = accumulatedLoss(operation, summarizer)
        assertEquals(ZERO, result)
    }

    @Test
    fun `when the operation results on gains should result on ZERO `() {

        val operation = Operation(
            operation = "sell",
            unitCost = BigDecimal("15.00"),
            quantity = BigDecimal("200")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("2000"),
            tax = listOf(),
            loss = BigDecimal("0")
        )

        val result = accumulatedLoss(
            operation,
            summarizer
        )

        assertEquals(ZERO, result)
    }

    @Test
    fun `when the operation results on loss should show the total of deficit `() {
        val operation = Operation(
            operation = "sell",
            unitCost = BigDecimal("5.00"),
            quantity = BigDecimal("2000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("2000"),
            tax = listOf(),
            loss = BigDecimal("0")
        )

        val result = accumulatedLoss(
            operation,
            summarizer
        )

        assertEquals(BigDecimal("10000.00"), result)
    }

    @Test
    fun `when the operation does not result on gains should result 0 on calculateGains`() {
        val result = calculateGains(
            BigDecimal("2.00"),
            BigDecimal("2.00"),
            BigDecimal("2")
        )
        assertEquals(ZERO, result)
    }

    @Test
    fun `when the operation does result on loss should result 0 on calculateGains`() {
        val result = calculateGains(
            BigDecimal("2.00"),
            BigDecimal("2.00"),
            BigDecimal("2")
        )
        assertEquals(ZERO, result)
    }

    @Test
    fun `when the operation results on gains should result the total of gains`() {
        val result = calculateGains(
            BigDecimal("12.00"),
            BigDecimal("2.00"),
            BigDecimal("2")
        )
        assertEquals(BigDecimal("20.00"), result)
    }

    @Test
    fun `when the operation results loss should return true`() {
        assertTrue { hasLoss(BigDecimal("1.00"), BigDecimal("2.00")) }
    }

    @Test
    fun `when the operation results no loss should return true`() {
        assertFalse { hasLoss(BigDecimal("2.00"), BigDecimal("1.00")) }
        assertFalse { hasLoss(BigDecimal("1.00"), BigDecimal("1.00")) }
    }

    @Test
    fun `given that when buy an stock with gains should return the weightedAveragePrice updated`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("25.00"),
            quantity = BigDecimal("5000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("10000"),
            tax = listOf(),
            loss = BigDecimal("0")
        )
        val result = calculateWeightedAveragePrice(summarizer, operation, BigDecimal("15000"))

        assertEquals(BigDecimal("15.00"), result)
    }

    @Test
    fun `given that when buy an stock with loss should return the weightedAveragePrice updated`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("5.00"),
            quantity = BigDecimal("8000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("20.00"),
            quantity = BigDecimal("500"),
            tax = listOf(),
            loss = BigDecimal("0")
        )
        val result = calculateWeightedAveragePrice(summarizer, operation, BigDecimal("15000"))

        assertEquals(BigDecimal("3.33"), result)
    }

    @Test
    fun `given that when buy an stock with cents should return the weightedAveragePrice updated`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("0.12"),
            quantity = BigDecimal("8000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("2.00"),
            quantity = BigDecimal("500"),
            tax = listOf(),
            loss = BigDecimal("0")
        )
        val result = calculateWeightedAveragePrice(summarizer, operation, BigDecimal("15000"))

        assertEquals(BigDecimal("0.13"), result)
    }

    @Test
    fun `given that is the first operation and there is no weightedAveragePrice should return the weightedAveragePrice based on first buy unit-cost`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("1.00"),
            quantity = BigDecimal("8000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = ZERO,
            quantity = ZERO,
            tax = listOf(),
            loss = ZERO
        )
        val result = calculateWeightedAveragePrice(summarizer, operation, BigDecimal("8000"))

        assertEquals(BigDecimal("1.00"), result)
    }

    @Test
    fun `when the operation there loss should return zero tax`() {
        val operation = Operation(
            operation = "sell",
            unitCost = BigDecimal("1.00"),
            quantity = BigDecimal("8000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("80"),
            tax = listOf(),
            loss = BigDecimal("8000.00")
        )

        assertEquals(ZERO.setScaleHalf(), calculateSellTax(summarizer, operation))
    }

    @Test
    fun `given that the operation there no loss or gains on transaction but with loss should return zero tax`() {
        val operation = Operation(
            operation = "sell",
            unitCost = BigDecimal("1.00"),
            quantity = BigDecimal("8000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("1.00"),
            quantity = BigDecimal("8000"),
            tax = listOf(),
            loss = BigDecimal("8000.00")
        )

        assertEquals(ZERO.setScaleHalf(), calculateSellTax(summarizer, operation))
    }


    @Test
    fun `When the operation is taxable and there are no accumulated losses, the tax value should be calculated as a proportion of the gains`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("25.00"),
            quantity = BigDecimal("5000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("10000"),
            tax = listOf(),
            loss = BigDecimal("0")
        )

        assertEquals(BigDecimal("15000.00"), calculateSellTax(summarizer, operation))
    }

    @Test
    fun `When the operation is taxable and there is accumulated losses, the tax value should be calculated with deduction of the loss`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("12.00"),
            quantity = BigDecimal("10000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("10000"),
            tax = listOf(),
            loss = BigDecimal("10000.00")
        )

        assertEquals(BigDecimal("2000.00"), calculateSellTax(summarizer, operation))
    }

    @Test
    fun `When the operation is taxable but the gains does not overcome the losses, the tax value should zero`() {
        val operation = Operation(
            operation = "buy",
            unitCost = BigDecimal("12.00"),
            quantity = BigDecimal("10000")
        )
        val summarizer = Summarizer(
            weightedAveragePrice = BigDecimal("10.00"),
            quantity = BigDecimal("10000"),
            tax = listOf(),
            loss = BigDecimal("500000.00")
        )

        assertEquals(ZERO.setScaleHalf(), calculateSellTax(summarizer, operation))
    }
}