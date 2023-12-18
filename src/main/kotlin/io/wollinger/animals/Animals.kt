package io.wollinger.animals

import io.wollinger.animals.utils.image
import org.w3c.dom.Image

enum class Animal(val size: Double, val image: Image) {
    PARROT(0.1, image("/img/round/parrot.png")),
    RABBIT(0.15, image("/img/round/rabbit.png")),
    SNAKE(0.25, image("/img/round/snake.png")),
    MONKEY(0.35, image("/img/round/monkey.png"))

}