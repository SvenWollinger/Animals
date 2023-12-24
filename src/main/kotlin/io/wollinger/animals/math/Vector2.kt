package io.wollinger.animals.math

import io.wollinger.animals.utils.toFixed
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
class Vector2(var x: Double = 0.0, var y: Double = 0.0) {
    fun distance(other: Vector2) = sqrt((other.y - this.y).pow(2) + (other.x - this.x).pow(2))
    fun set(x: Double = this.x, y: Double = this.y) {
        this.x = x
        this.y = y
    }

    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun div(n: Int): Vector2 {
        return Vector2(x / n, y / n)
    }

    override fun equals(other: Any?): Boolean {
        return other is Vector2 && x == other.x && y == other.y
    }

    fun limit(maxX: Double, maxY: Double) {
        if(x > maxX) x = maxX
        if(y > maxY) y = maxY
    }
    fun min(minX: Double, minY: Double) {
        if(x < minX) x = minX
        if(y < minY) y = minY

    }
    override fun toString() = "Vector2(x=${x.toFixed(2)}, y=${y.toFixed(2)})"
    override fun hashCode() = 31 * x.hashCode() + y.hashCode()

    companion object {
        fun fromDynamic(vector2: dynamic) = Vector2(vector2.x as Double, vector2.y as Double)
    }
}