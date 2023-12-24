package io.wollinger.animals.screens

import io.wollinger.animals.input.Input
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class NoScreen: Screen {
    private val text = "No screen loaded"
    override fun update(delta: Double, canvas: HTMLCanvasElement, input: Input) { }

    override fun render(delta: Double, canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D) {
        val textSize = canvas.height / 10
        ctx.fillStyle = "black"
        ctx.font = "${textSize}px Roboto Mono"
        ctx.fillText(text, canvas.width / 2 - ctx.measureText(text).width / 2, canvas.height / 2 - textSize / 2.0)
    }
}