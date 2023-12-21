package io.wollinger.animals.utils

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

    fun remove(vararg body: dynamic) {
        body.forEach {
            _matter.Composite.remove(engine.world, it)
        }
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

    fun addCircle(label: String = "circle", x: Double, y: Double, radius: Double, detail: Int = 50) {
        val body = _matter.Bodies.circle(x, y, radius, detail)
        body.label = label
        _matter.Composite.add(engine.world, arrayOf(body))
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
    val vertices: Array<dynamic>,
    val ref: dynamic
) {
    companion object {
        fun fromDynamic(body: dynamic): Body {
            return Body(body.id, body.label, Vector2(body.position.x, body.position.y), body.circleRadius, body.angle, body.vertices, body)
        }
    }
}