package io.wollinger.animals.screens

import io.wollinger.animals.input.Input
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

interface Screen {
    fun update(delta: Double, canvas: HTMLCanvasElement, input: Input)
    fun render(delta: Double, canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D)
}