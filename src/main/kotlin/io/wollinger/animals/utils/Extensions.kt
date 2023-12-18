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

fun dynamicToCollisionEvent(event: dynamic): CollisionActiveEvent {
    val list = ArrayList<CollisionActiveEventPair>()
    event.pairs.forEach { pair ->
        list.add(CollisionActiveEventPair(
            bodyToPhysicsBody(pair.bodyA),
            bodyToPhysicsBody(pair.bodyB)
        ))
    }
    return CollisionActiveEvent(list)

}

fun bodyToPhysicsBody(body: dynamic) = PhysicsBody(body.id, Vector2(body.position.x, body.position.y), body)

data class CollisionActiveEvent(
    val pairs: List<CollisionActiveEventPair>
)

data class CollisionActiveEventPair(
    val bodyA: PhysicsBody,
    val bodyB: PhysicsBody
)

fun <T> HashMap<T, *>.removeAll(vararg keys: T) {
    keys.forEach {
        remove(it)
    }
}

data class PhysicsBody(val id: Int, val position: Vector2, val bodyRef: dynamic)