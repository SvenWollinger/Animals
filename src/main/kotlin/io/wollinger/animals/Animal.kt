package io.wollinger.animals

import org.w3c.dom.Image

data class Animal(
    val size: Int,
    val image: Image
)

val AnimalDB = arrayOf(
    Animal(0, Image().apply { src = "/img/round/rabbit.png" }),
    Animal(1, Image().apply { src = "/img/round/snake.png" })
)