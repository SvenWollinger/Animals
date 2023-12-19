package io.wollinger.animals

import io.wollinger.animals.utils.image
import org.w3c.dom.Image

enum class Animal(val size: Double, val image: Image) {
    PARROT(0.2, image("/img/round/parrot.png")),
    RABBIT(0.3, image("/img/round/rabbit.png")),
    SNAKE(0.5, image("/img/round/snake.png")),
    MONKEY(0.8, image("/img/round/monkey.png")),
    PIG(1.0, image("/img/round/pig.png")),
    PENGUIN(1.15, image("/img/round/penguin.png")),
    PANDA(1.3, image("/img/round/panda.png")),
    WALRUS(1.5, image("/img/round/walrus.png"))


}