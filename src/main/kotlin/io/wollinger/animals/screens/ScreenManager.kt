package io.wollinger.animals.screens

import io.wollinger.animals.input.Input
import io.wollinger.animals.utils.fillRect
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event

class ScreenManager(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D,
) {
    private val input = Input()
    private val noScreen = NoScreen()
    var screen: Screen? = null

    init {
        val callback: (Event) -> Unit = { input.handle(it) }
        window.addEventListener(type = "keydown", options = false, callback = callback)
        window.addEventListener(type = "keyup", options = false, callback = callback)
        window.addEventListener(type = "mousedown", options = false, callback = callback)
        window.addEventListener(type = "mouseup", options = false, callback = callback)
        window.addEventListener(type = "mousemove", options = false, callback = callback)
        window.addEventListener(type = "touchstart", options = false, callback = callback)
        window.addEventListener(type = "touchend", options = false, callback = callback)

        window.requestAnimationFrame(::loop)
    }

    private var lastRender = 0.0
    private fun loop(timestamp: Double) {
        if(canvas.width != window.innerWidth || canvas.height != window.innerHeight) {
            canvas.width = window.innerWidth
            canvas.height = window.innerHeight
            ctx.imageSmoothingEnabled = true
        }

        ctx.fillStyle = "white"
        ctx.fillRect(0, 0, canvas.width, canvas.height)

        val delta = (timestamp - lastRender).coerceIn(0.0, 20.0)
        val useScreen = screen ?: noScreen
        useScreen.update(delta, canvas, input)
        useScreen.render(delta, canvas, ctx)

        lastRender = timestamp
        window.requestAnimationFrame(::loop)
    }
}