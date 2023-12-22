package io.wollinger.animals.utils

import kotlin.js.json

@JsModule("matter-js")
@JsNonModule
private external val _matter: dynamic

class Matter {
    private val engine = _matter.Engine.create()

    var timescale: Double
        get() = engine.timing.timescale as Double
        set(value) = run { engine.timing.timeScale = value }

    fun update(delta: Double) {
        _matter.Engine.update(engine, delta)
    }

    fun addRectangle(label: String = "rectangle", x: Int, y: Int, width: Int, height: Int, isStatic: Boolean = false) {
        val body = _matter.Bodies.rectangle(x, y, width, height)
        body.isStatic = isStatic
        body.label = label
        _matter.Composite.add(engine.world, arrayOf(body))
    }

    fun getBodies(): List<Body> {
        val temp = _matter.Composite.allBodies(engine.world) as Array<dynamic>
        return temp.map { Body.fromDynamic(it) }.toList()
    }

    fun remove(vararg body: Body) {
        body.map { it.ref }.forEach { _matter.Composite.remove(engine.world, it) }
    }

    fun onCollisionStart(action: (CollisionEvent) -> Unit) {
        _matter.Events.on(engine, "collisionStart") { event  ->
            val pairs = (event.pairs as Array<dynamic>).map {
                Pair(
                    Body.fromDynamic(it.bodyA),
                    Body.fromDynamic(it.bodyB)
                )
            }
            action.invoke(CollisionEvent(pairs))
        }
    }

    fun addCircle(label: String = "circle", x: Double, y: Double, radius: Double, detail: Int = 50, angle: Double = 0.0, velocity: Vector2 = Vector2()) {
        val body = _matter.Bodies.circle(x, y, radius, detail)
        body.label = label
        body.restitution = 0.3
        _matter.Body.rotate(body, angle)
        _matter.Body.setMass(body, 0.5 * radius)
        _matter.Composite.add(engine.world, arrayOf(body))
        _matter.Body.setVelocity(body, json(Pair("x", velocity.x), Pair("y", velocity.y)))
    }
}

data class CollisionEvent(
    val pairs: List<Pair<Body, Body>>
)

data class Body(
    val id: Int,
    val label: String,
    val position: Vector2,
    val circleRadius: Double = 0.0,
    val angle: Double = 0.0,
    val vertices: List<Vector2>,
    val velocity: Vector2,
    val ref: dynamic
) {
    companion object {
        fun fromDynamic(body: dynamic): Body {
            return Body(
                id = body.id as Int,
                label = body.label as String,
                position = Vector2.fromDynamic(body.position),
                circleRadius = body.circleRadius as Double,
                angle = body.angle as Double,
                vertices = (body.vertices as Array<dynamic>).map { Vector2.fromDynamic(it) },
                velocity = Vector2.fromDynamic(body.velocity),
                ref = body
            )
        }
    }
}