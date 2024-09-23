import org.example.calculations.isTaxable
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaxCalculationsTests {

    @Test
    fun `when the profit's value is bigger than limit should be taxable`() {
        assertTrue { isTaxable(BigDecimal(2000000)) }
    }

    @Test
    fun `when the profit's value is less than limit should be taxable`() {
        assertFalse { isTaxable(BigDecimal(200)) }
    }
}