package io.wollinger.animals.math

import io.wollinger.animals.utils.toFixed
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
class Vector2(var x: Double = 0.0, var y: Double = 0.0) {
    constructor(x: Number, y: Number): this(x.toDouble(), y.toDouble())

    operator fun plus(vector: Vector2)  = Vector2(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector2) = Vector2(x - vector.x, y - vector.y)
    operator fun div(n: Int)            = Vector2(x / n, y / n)
    operator fun times(n: Int)          = Vector2(x * n, y * n)

    fun limit(maxX: Number, maxY: Number) = limit(maxX.toDouble(), maxY.toDouble())
    private fun limit(maxX: Double, maxY: Double) {
        if(x > maxX) x = maxX
        if(y > maxY) y = maxY
    }

    fun min(minX: Number, minY: Number) = min(minX.toDouble(), minY.toDouble())
    private fun min(minX: Double, minY: Double) {
        if(x < minX) x = minX
        if(y < minY) y = minY
    }

    fun dst(other: Vector2) = sqrt((other.y - this.y).pow(2) + (other.x - this.x).pow(2))

    fun set(x: Number = this.x, y: Number = this.y) {
        this.x = x.toDouble()
        this.y = y.toDouble()
    }

    override fun hashCode() = 31 * x.hashCode() + y.hashCode()
    override fun equals(other: Any?) = other is Vector2 && x == other.x && y == other.y

    override fun toString() = "Vector2(x=${x.toFixed(2)}, y=${y.toFixed(2)})"

    companion object {
        fun fromDynamic(vector2: dynamic) = Vector2(vector2.x as Double, vector2.y as Double)
    }
}