package io.wollinger.animals

import io.wollinger.animals.screens.MenuScreen
import io.wollinger.animals.screens.ScreenManager
import kotlinx.browser.document
import org.w3c.dom.*

fun main() {
    val gameElement = document.getElementsByTagName("game")[0]
    if(gameElement == null) {
        console.error("<game> tag not found! Aborting...")
        return
    }

    val canvas = document.createElement("canvas").apply {
        gameElement.appendChild(this)
    } as HTMLCanvasElement

    val screenManager = ScreenManager(canvas, (canvas.getContext("2d") as CanvasRenderingContext2D))
    screenManager.screen = MenuScreen(screenManager)
}