package io.wollinger.animals.utils

data class Rectangle(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var width: Double = 0.0,
    var height: Double = 0.0
) {
    fun from(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
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
