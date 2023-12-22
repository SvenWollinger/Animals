package io.wollinger.animals

import io.wollinger.animals.utils.image
import org.w3c.dom.Audio

object Resources {
    val COIN = image("/img/coin.png")
    val CLOUDS = listOf("cloud0", "cloud1", "cloud2").map { image("/img/$it.png") }
    val PARROT  = r("parrot")
    val RABBIT  = r("rabbit")
    val SNAKE   = r("snake")
    val MONKEY  = r("monkey")
    val PIG     = r("pig")
    val PENGUIN = r("penguin")
    val PANDA   = r("panda")
    val WALRUS  = r("walrus")

    val FENCE = image("/img/ladder_mid.png")
    val GRASS = image("/img/grass.png")

    val POOF = Audio("/sound/poof.ogg")
    val TOUCH = Audio("/sound/touch.mp3")

    private fun r(t: String) = image("/img/round/$t.png")
}