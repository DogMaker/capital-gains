package org.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.example.models.Operation
import java.io.File


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


fun main() {
    val file = File("C:\\Users\\55119\\IdeaProjects\\capital-gains\\src\\main\\resources\\capital.txt")

    val mapper = jacksonObjectMapper()

    file.forEachLine { line ->
        val list: List<Operation> = mapper.readValue(line)

        println(mapper.writeValueAsString(calculate(list)))
    }
}





