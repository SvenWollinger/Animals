package io.wollinger.animals

import io.wollinger.animals.math.Vector2
import kotlin.js.json

@JsModule("matter-js")
@JsNonModule
private external val matterjs: dynamic

class Matter {
    private val engine = matterjs.Engine.create()

    var timescale: Double
        get() = engine.timing.timescale as Double
        set(value) = run { engine.timing.timeScale = value }

    fun update(delta: Double) {
        matterjs.Engine.update(engine, delta)
    }

    fun addRectangle(label: String = "rectangle", x: Int, y: Int, width: Int, height: Int, isStatic: Boolean = false) {
        val body = matterjs.Bodies.rectangle(x, y, width, height)
        body.isStatic = isStatic
        body.label = label
        matterjs.Composite.add(engine.world, arrayOf(body))
    }

    fun getBodies(): List<Body> {
        return (matterjs.Composite.allBodies(engine.world) as Array<dynamic>).asList().map { Body.fromDynamic(it) }
    }

    fun remove(vararg body: Body) {
        fun remove(body: Body) { matterjs.Composite.remove(engine.world, body) }
        body.map { it.ref }.forEach(::remove)
    }

    fun onCollisionStart(action: (CollisionEvent) -> Unit) {
        matterjs.Events.on(engine, "collisionStart") { event  ->
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
        val body = matterjs.Bodies.circle(x, y, radius, detail)
        body.label = label
        body.restitution = 0.3
        matterjs.Body.rotate(body, angle)
        matterjs.Body.setMass(body, 0.5 * radius)
        matterjs.Composite.add(engine.world, arrayOf(body))
        matterjs.Body.setVelocity(body, json(Pair("x", velocity.x), Pair("y", velocity.y)))
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