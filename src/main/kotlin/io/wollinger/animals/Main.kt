package io.wollinger.animals

import io.wollinger.animals.utils.launch
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

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
    screenManager.screen = GameScreen()
}