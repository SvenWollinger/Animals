package io.wollinger.animals.math

data class Rectangle(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0
) {
    constructor(x: Number, y: Number, width: Number, height: Number) : this(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

    fun from(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun contains(vector2: Vector2): Boolean {
        return (vector2.x >= x && vector2.y >= y && vector2.x <= x + width && vector2.y <= y + height)
    }

    fun intersects(other: Rectangle): Boolean {
        return !(
                ((y + height) < (other.y)) ||
                        (y > (other.y + other.height)) ||
                        ((x + width) < other.x) ||
                        (x > (other.x + other.width))
                );
    }
}
