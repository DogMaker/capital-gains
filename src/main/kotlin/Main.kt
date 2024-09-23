package org.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.example.models.Operation
import org.example.services.calculateTax
import java.io.File


fun main() {
    val lines = generateSequence(::readLine).toList()
    lines.forEach { println(it) }

    val file = File("C:\\Users\\55119\\IdeaProjects\\capital-gains\\src\\main\\resources\\capital.txt")

    val mapper = jacksonObjectMapper()

    file.forEachLine { line ->
        val list: List<Operation> = mapper.readValue(line)

        println(mapper.writeValueAsString(calculateTax(list)))
    }
}





