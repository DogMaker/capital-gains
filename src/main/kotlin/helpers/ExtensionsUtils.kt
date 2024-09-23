package org.example.helpers

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.setScaleDown(scale: Int = 2): BigDecimal = this.setScale(scale, RoundingMode.DOWN)
