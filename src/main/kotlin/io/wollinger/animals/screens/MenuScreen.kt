package io.wollinger.animals.screens

import io.wollinger.animals.input.Button
import io.wollinger.animals.input.Input
import io.wollinger.animals.math.Rectangle
import io.wollinger.animals.utils.fillRect
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

class MenuScreen(private val screenManager: ScreenManager): Screen {
    data class UIButton(val text: String, val rect: Rectangle = Rectangle(), var hover: Boolean = false, val action: () -> Unit)

    private val playButton = UIButton("Play") { screenManager.screen = GameScreen() }

    override fun update(delta: Double, canvas: HTMLCanvasElement, input: Input) {
        playButton.rect.width = canvas.width / 2.0
        playButton.rect.height = canvas.height / 2.0
        playButton.rect.x = canvas.width / 2 - playButton.rect.width / 2
        playButton.rect.y = canvas.height / 2 - playButton.rect.height / 2
        if(input.isPressed(Button.MOUSE_LEFT) && playButton.rect.contains(input.mousePos)) playButton.action.invoke()
        playButton.hover = playButton.rect.contains(input.mousePos)
    }

    override fun render(delta: Double, canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D) {
        ctx.fillStyle = if(playButton.hover) "lightblue" else "blue"
        ctx.fillRect(playButton.rect)

        val fontSize = playButton.rect.height / 2
        val textWidth = ctx.measureText("Play").width

        ctx.font = "${fontSize}px Roboto Mono"
        ctx.fillStyle = "black"
        ctx.fillText("Play", playButton.rect.x + playButton.rect.width / 2 - textWidth / 2, playButton.rect.y + playButton.rect.height / 2 + fontSize / 2)
    }
}