package io.wollinger.animals

import kotlinx.browser.document
import kotlinx.browser.window
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
    val input = Input()
    val callback : (Event) -> Unit = {
        if(it is KeyboardEvent)
            input.handle(it)
    }
    window.addEventListener(type = "keydown", options = false, callback = callback)
    window.addEventListener(type = "keyup", options = false, callback = callback)

    val ctx = (canvas.getContext("2d") as CanvasRenderingContext2D)
    Engine(canvas, ctx, input)

}