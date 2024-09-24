package org.capital.gains.helpers

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.setScaleHalf(scale: Int = 2): BigDecimal = this.setScale(scale, RoundingMode.HALF_EVEN)
