import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.example.services.calculateTax
import org.example.models.Operation
import org.example.models.Tax
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals


class OperationsTests{
    @Test
    fun `when operation has profit over 20000 without loss should return with tax`() {
    val operationsInput = """
        [{"operation":"buy", "unit-cost":10.00, "quantity": 10000}, 
        {"operation":"sell", "unit-cost":20.00, "quantity": 5000}]
        """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":10000.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when operation has no profit should return with no tax`() {
        val operationsInput = """
            [{"operation":"buy", "unit-cost":20.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":10.00, "quantity": 5000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when operation has profit under 20000 should return with no tax`() {
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 100},
            {"operation":"sell", "unit-cost":15.00, "quantity": 50},
            {"operation":"sell", "unit-cost":15.00, "quantity": 50}]
            """.trimIndent()
        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when has two sell operations one with profit and other with loss should tax with profit and not  to tax the sell with loss`() {
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":20.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":5.00, "quantity": 5000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":10000.00},{"tax":0.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when receives two lines should treat as independent simulations without carrying state between executions`() {
        val path = Paths.get(ClassLoader.getSystemResource("capital-test.txt").toURI())
        val operationsInput =  File(path.toString())

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00}][{"tax":0.00},{"tax":10000.00},{"tax":0.00}]""".trimIndent()

        var result = ""
        val mapper = jacksonObjectMapper()

        operationsInput.forEachLine { line ->
            val inputs: List<Operation> = mapper.readValue(line)
            result = "$result${calculateTax(inputs).writeString()}"
        }

        assertEquals(expectedResult, result)
    }


    @Test
    fun `when there is a loss on first sell and another sell, there is a profit should deduct losses and calculate the correct tax`(){
        val operationsInput = """
            [{"operation":"buy","unit-cost":10.00,"quantity":10000},
            {"operation":"sell","unit-cost":5.00,"quantity":5000},
            {"operation":"sell","unit-cost":20.00,"quantity":3000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":1000.00}]""".trimIndent()

        val operations = operationsInput.read()
        val result = calculateTax(operations).writeString()

        assertEquals(expectedResult, result)
    }


    @Test
    fun `when buying operations followed by sell operation with average price should have no tax due to no profit or loss`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"buy", "unit-cost":25.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":15.00, "quantity": 10000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00}]""".trimIndent()

        val operations= operationsInput.read()
        val result = calculateTax(operations).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when following with two sell orders, the first one with no losses or gains and the second with a profit, should be calculated the tax correct`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"buy", "unit-cost":25.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":15.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":25.00, "quantity": 5000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00},{"tax":10000.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `given operations with initial loss followed by partial profit when no more loss the final profit should deduct the loss and correctly apply the tax`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":2.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
            {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
            {"operation":"sell", "unit-cost":25.00, "quantity": 1000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00},{"tax":0.00},{"tax":3000.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }


    @Test
    fun `when buying and selling operations with losses and profits followed by new buy and sells should correctly calculate tax based on weighted average and deductions`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":2.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
            {"operation":"sell", "unit-cost":20.00, "quantity": 2000},
            {"operation":"sell", "unit-cost":25.00, "quantity": 1000},
            {"operation":"buy", "unit-cost":20.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":15.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":30.00, "quantity": 4350},
            {"operation":"sell", "unit-cost":30.00, "quantity": 650}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00},{"tax":0.00},{"tax":3000.00},{"tax":0.00},{"tax":0.00},{"tax":3700.00},{"tax":0.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }


    @Test
    fun `when buying and selling operations gain high profits should calculate tax on each profit based on entire amount`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":50.00, "quantity": 10000},
            {"operation":"buy", "unit-cost":20.00, "quantity": 10000},
            {"operation":"sell", "unit-cost":50.00, "quantity": 10000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":80000.00},{"tax":0.00},{"tax":60000.00}]""".trimIndent()

        val inputs = operationsInput.read()
        val result = calculateTax(inputs).writeString()

        assertEquals(expectedResult, result)
    }

    private fun String.read(): List<Operation> = jacksonObjectMapper().readValue(this)
    private fun List<Tax>.writeString(): String = jacksonObjectMapper().writeValueAsString(this)
}
