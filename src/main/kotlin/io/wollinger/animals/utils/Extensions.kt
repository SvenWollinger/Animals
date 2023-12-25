package io.wollinger.animals.utils

import io.wollinger.animals.math.Rectangle
import io.wollinger.animals.math.Vector2
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Image
import kotlin.js.Date

fun CanvasRenderingContext2D.use(translateX: Double = 0.0, translateY: Double = 0.0, angle: Double = 0.0, action: CanvasRenderingContext2D.() -> Unit) {
    translate(translateX, translateY)
    rotate(angle)
    action.invoke(this)
    rotate(-angle)
    translate(-translateX, -translateY)
}

fun image(src: String) = Image().apply { this.src = src }

fun <T> HashMap<T, *>.removeAll(vararg keys: T) {
    keys.forEach {
        remove(it)
    }
}

fun <T> Collection<T>.containsAny(vararg args: T): Boolean {
    args.forEach {
        if(contains(it)) return true
    }
    return false
}

fun <T> ArrayList<T>.removeFirst(intRange: IntRange) {
    subList(intRange.first, intRange.last).clear()
}

fun <T> ArrayList<T>.limitFirst(limit: Int) {
    if(limit >= size) return
    subList(0, size - limit).clear()
}

fun <T> ArrayList<T>.addAll(vararg args: T) {
    addAll(args.toList())
}

fun CanvasRenderingContext2D.fillRect(x: Number, y: Number, width: Number, height: Number) = fillRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())
fun CanvasRenderingContext2D.moveTo(vector2: Vector2) = moveTo(vector2.x, vector2.y)
fun CanvasRenderingContext2D.lineTo(vector2: Vector2) = lineTo(vector2.x, vector2.y)
fun CanvasRenderingContext2D.trace(points: List<Vector2>) {
    moveTo(points[0])
    points.forEach { lineTo(it) }
    lineTo(points[0])
}
fun CanvasRenderingContext2D.rect(rectangle: Rectangle) = rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
fun CanvasRenderingContext2D.fillRect(rectangle: Rectangle) = fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
fun CanvasRenderingContext2D.drawImage(image: Image, rectangle: Rectangle) = drawImage(image, rectangle.x, rectangle.y, rectangle.width, rectangle.height)

fun Date.prettyString(): String {
    val day = getDate()
    val month = getMonth() + 1
    val year = getFullYear()
    val minute = if(getMinutes() > 9) getMinutes() else "0${getMinutes()}"
    val hours = if(getHours() > 9) getHours() else "0${getHours()}"
    return "$day.$month.$year $hours:$minute"
}