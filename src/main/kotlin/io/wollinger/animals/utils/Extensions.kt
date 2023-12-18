package io.wollinger.animals.utils

import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Image

fun CanvasRenderingContext2D.use(translateX: Double = 0.0, translateY: Double = 0.0, angle: Double = 0.0, action: CanvasRenderingContext2D.() -> Unit) {
    translate(translateX, translateY)
    rotate(angle)
    action.invoke(this)
    rotate(-angle)
    translate(-translateX, -translateY)
}

fun image(src: String) = Image().apply { this.src = src }