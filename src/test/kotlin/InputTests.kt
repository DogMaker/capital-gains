import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.example.calculate
import org.example.models.Operation
import org.example.models.Tax
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertEquals


class InputTests{
    @Test
    fun when_operation_has_profit_over_20000_should_return_with_tax() {
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000}, {\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 5000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":10000.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_operation_has_no_profit_should_return_with_no_tax() {
        val input = "[{\"operation\":\"buy\", \"unit-cost\":20.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":10.00, \"quantity\": 5000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_operation_has_profit_under_20000_should_return_with_no_tax() {
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 100},{\"operation\":\"sell\", \"unit-cost\":15.00, \"quantity\": 50},{\"operation\":\"sell\", \"unit-cost\":15.00, \"quantity\": 50}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_has_two_sell_operations_one_with_profit_over_20000_and_other_with_loss_should_tax_with_profit_and_no_tax_loss() {
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 5000},{\"operation\":\"sell\", \"unit-cost\":5.00, \"quantity\": 5000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":10000.00},{\"tax\":0.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_receives_two_lines_should_treat_as_independent_simulations_without_carrying_state_between_executions() {
        val path = Paths.get(ClassLoader.getSystemResource("capital-test.txt").toURI())
        val input =  File(path.toString())

        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00}][{\"tax\":0.00},{\"tax\":10000.00},{\"tax\":0.00}]"

        var result : String = ""
        val mapper = jacksonObjectMapper()

        input.forEachLine { line ->
            val inputs: List<Operation> = mapper.readValue(line)
            result = "$result${jacksonObjectMapper().writeValueAsString(calculate(inputs))}"
        }

        assertEquals(expectedResult, result)
    }

    @Test
    fun `when buy operation then loss on first sell followed by second sell with deducted loss should calculate correct tax`(){
        val operationsInput = """
            [{"operation":"buy","unit-cost":10.00,"quantity":10000},
            {"operation":"sell","unit-cost":5.00,"quantity":5000},
            {"operation":"sell","unit-cost":20.00,"quantity":3000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":1000.00}]"""".trimIndent()

        val operations = operationsInput.read()
        val result = calculate(operations).writeString()

        assertEquals(expectedResult, result)

    }

    @Test
    fun `when buy operations followed by sell operation with average price should have no tax due to no profit or loss`(){
        val operationsInput = """
            [{"operation":"buy", "unit-cost":10.00, "quantity": 10000},
            {"operation":"buy", "unit-cost":25.00, "quantity": 5000},
            {"operation":"sell", "unit-cost":15.00, "quantity": 10000}]
            """.trimIndent()

        val expectedResult = """[{"tax":0.00},{"tax":0.00},{"tax":0.00}]""".trimIndent()

        val operations= operationsInput.read()
        val result = calculate(operations).writeString()

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_buy_operations_with_unset_then_second_buy_followed_by_two_sell_operations_should_calculate_correct_tax_based_on_weighted_average_price(){
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000},{\"operation\":\"buy\", \"unit-cost\":25.00, \"quantity\": 5000},{\"operation\":\"sell\", \"unit-cost\":15.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":25.00, \"quantity\": 5000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00},{\"tax\":10000.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_operations_with_initial_loss_followed_by_partial_profit_then_no_more_loss_and_final_profit_should_correctly_apply_tax_after_loss_is_deducted(){
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":2.00, \"quantity\": 5000},{\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 2000},{\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 2000},{\"operation\":\"sell\", \"unit-cost\":25.00, \"quantity\": 1000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00},{\"tax\":3000.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_buy_and_sell_operations_with_losses_and_profits_followed_by_new_buy_and_sells_should_correctly_calculate_tax_based_on_weighted_average_and_deductions(){
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":2.00, \"quantity\": 5000},{\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 2000},{\"operation\":\"sell\", \"unit-cost\":20.00, \"quantity\": 2000},{\"operation\":\"sell\", \"unit-cost\":25.00, \"quantity\": 1000},{\"operation\":\"buy\", \"unit-cost\":20.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":15.00, \"quantity\": 5000},{\"operation\":\"sell\", \"unit-cost\":30.00, \"quantity\": 4350},{\"operation\":\"sell\", \"unit-cost\":30.00, \"quantity\": 650}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00},{\"tax\":0.00},{\"tax\":3000.00},{\"tax\":0.00}, {\"tax\":0.00},{\"tax\":3700.00},{\"tax\":0.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    @Test
    fun when_buy_and_sell_operations_with_high_profits_should_correctly_calculate_tax_on_each_profit_based_on_full_amount(){
        val input = "[{\"operation\":\"buy\", \"unit-cost\":10.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":50.00, \"quantity\": 10000},{\"operation\":\"buy\", \"unit-cost\":20.00, \"quantity\": 10000},{\"operation\":\"sell\", \"unit-cost\":50.00, \"quantity\": 10000}]"
        val expectedResult = "[{\"tax\":0.00},{\"tax\":80000.00},{\"tax\":0.00},{\"tax\":60000.00}]"

        val inputs: List<Operation> = jacksonObjectMapper().readValue(input)
        val result = jacksonObjectMapper().writeValueAsString(calculate(inputs))

        assertEquals(expectedResult, result)
    }

    private fun String.read(): List<Operation> = jacksonObjectMapper().readValue(this)
    private fun List<Tax>.writeString(): String = jacksonObjectMapper().writeValueAsString(this)
}
