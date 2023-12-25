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
    val COW = r("cow")
    val PANDA   = r("panda")
    val WALRUS  = r("walrus")
    val WHALE          = r("whale")

    val FENCE = image("/img/ladder_mid.png")
    val GRASS = image("/img/grass.png")
    val ARROW_RIGHT = image("/img/arrowRight.png")

    val POOF = Audio("/sound/poof.ogg")
    val TOUCH = Audio("/sound/touch.mp3")

    val COVER = image("/img/ui/cover.png")
    val BACK_BUTTON = image("/img/ui/buttons/back.png")
    val BACK_BUTTON_H = image("/img/ui/buttons/back-hover.png")
    val PLAY_BUTTON = image("/img/ui/buttons/play.png")
    val PLAY_BUTTON_H = image("/img/ui/buttons/play-hover.png")
    val ABOUT_BUTTON = image("/img/ui/buttons/about.png")
    val ABOUT_BUTTON_H = image("/img/ui/buttons/about-hover.png")
    val KENNEY_LOGO = image("/img/ui/buttons/kenney.png")
    val MATTERJS_LOGO = image("/img/ui/buttons/matterjs.png")
    val KOTLIN_LOGO = image("/img/ui/buttons/kotlin.png")

    val ABOUT = image("/img/ui/about.png")

    private fun r(t: String) = image("/img/round/$t.png")
}