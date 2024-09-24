package org.capital.gains


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.capital.gains.models.Operation
import org.capital.gains.services.calculateTax


const val END_ARRAY_CHAR = "]"
const val EMPTY = ""

fun main() {
    val file = generateSequence(::readLine).toList()
    val mapper = jacksonObjectMapper()

    fun processBlock(jsonBlock: String): String {
        val list: List<Operation> = mapper.readValue(jsonBlock)
        return mapper.writeValueAsString(calculateTax(list))
    }

    val results = file.fold(Pair(EMPTY, emptyList<String>())) { (accumulatedBlock, results), line ->
        val newBlock = accumulatedBlock + line.trim()

        if (line.trim().endsWith(END_ARRAY_CHAR)) {
            val result = processBlock(newBlock)
            Pair(EMPTY, results + result)
        } else {
            Pair(newBlock, results)
        }
    }.second

    results.asReversed().forEach { println(it) }
}

