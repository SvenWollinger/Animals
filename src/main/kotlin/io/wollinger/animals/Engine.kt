package io.wollinger.animals

import io.wollinger.animals.utils.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.js.Date

class Engine(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D,
    private val input: Input
) {
    private val engine = Matter.Engine.create()
    private lateinit var next: Animal

    private fun newAnimal() {
        next = Animal.values().copyOfRange(0, 3).random()
    }

    init {
        newAnimal()
    }

    private val fpsCounter = FPSCounter()

    private fun addAnimal(animal: Animal, x: Double) {
        addAnimal(animal, x * Const.BOARD_VIRT_WIDTH, 0.0)
    }

    private fun addAnimal(animal: Animal, x: Double, y: Double) {
        val body = Matter.Bodies.circle(x, y, animal.size * Const.ANIMAL_SCALE, { }, 50)
        body.label = animal.name
        Matter.Composite.add(engine.world, arrayOf(body))
    }

    private var aX = 0.0
    private var lastClick = Date.now()

    private val timeout = 300
    init {
        window.addEventListener(type = "mousedown", options = false, callback = {
            if(lastClick + timeout > Date.now()) return@addEventListener
            it as MouseEvent
            val boardHeight = window.innerHeight * 0.8
            val boardWidth = boardHeight * Const.BOARD_VIRT_DIFF
            val test = ((it.x - 64) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(next, test)
            newAnimal()
            lastClick = Date.now()
        })



        Matter.Events.on(engine, "collisionStart") { event  ->
            dynamicToCollisionEvent(event).pairs.forEach {
                if(it.bodyA.label == "wall" || it.bodyB.label == "wall") return@forEach
                val a = Animal.valueOf(it.bodyA.label)
                val b = Animal.valueOf(it.bodyB.label)

                if(a == b) {
                    val middle = (it.bodyA.position + it.bodyB.position) / 2
                    //animals.removeAll(it.bodyA.id, it.bodyB.id)

                    Matter.Composite.remove(engine.world, it.bodyA.bodyRef)
                    Matter.Composite.remove(engine.world, it.bodyB.bodyRef)

                    val next = Animal.values().indexOf(a) + 1
                    if(next >= Animal.values().size) {
                        window.alert("You won!")
                        document.location!!.reload()
                    } else {
                        addAnimal(Animal.values()[next], middle.x, middle.y)
                    }
                }
            }

        }


        window.addEventListener(type = "mousemove", options = false, callback = {
            it as MouseEvent
            val boardHeight = window.innerHeight * 0.8
            val boardWidth = boardHeight * Const.BOARD_VIRT_DIFF
            aX = (it.x - 64) / boardWidth
        })

        fun wall(x: Int, y: Int, width: Int, height: Int): dynamic {
            val body = Matter.Bodies.rectangle(x + width / 2, y + height / 2, width, height)
            body.isStatic = true
            body.isSensor = false
            body.label = "wall"
            return body
        }
        val t = Const.BOARD_VIRT_WALL_THICKNESS
        val w = Const.BOARD_VIRT_WIDTH
        val h = Const.BOARD_VIRT_HEIGHT
        Matter.Composite.add(engine.world, arrayOf(
            wall(-t, 0, t, h),
            wall(w, 0, t, h),
            wall(0, h, w, t),
        ))


        window.requestAnimationFrame(::loop)
    }

    private fun update(delta: Double) {
        Matter.Engine.update(engine, delta)
    }

    private fun size() = canvas.height / 16.0

    private fun draw() {
        if(canvas.width != window.innerWidth || canvas.height != window.innerHeight) {
            canvas.width = window.innerWidth
            canvas.height = window.innerHeight
            ctx.imageSmoothingEnabled = false
        }
        val size = size()

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0.0, 0.0, window.innerWidth.toDouble(), window.innerHeight.toDouble())

        val boardHeight = window.innerHeight * 0.8
        val boardWidth = boardHeight * Const.BOARD_VIRT_DIFF

        val offsetX = 64.0
        val offsetY = 64.0



        (Matter.Composite.allBodies(engine.world) as Array<dynamic>).forEach { body ->
            val pBody = bodyToPhysicsBody(body)
            if(pBody.label == "wall") return@forEach
            val animal = Animal.valueOf(pBody.label)

            val x = ((pBody.position.x) / Const.BOARD_VIRT_WIDTH) * boardWidth
            val y = ((pBody.position.y) / Const.BOARD_VIRT_HEIGHT) * boardHeight
            ctx.use(
                translateX = x + offsetX,
                translateY = y + offsetY,
                angle = body.angle as Double
            ) {
                val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth) as Double
                drawImage(animal.image, -radius, -radius, radius * 2, radius * 2)
            }

        }

        if(!input.isPressed("f")) {
            ctx.translate(offsetX, offsetY)
            ctx.fillStyle = "black"
            ctx.strokeStyle = "black"
            ctx.beginPath()

            (Matter.Composite.allBodies(engine.world) as Array<dynamic>).forEach { body ->
                data class Vert(val x: Double, val y: Double)
                fun vert(vertice: dynamic): Vert {
                    val nX = ((vertice.x as Double) / Const.BOARD_VIRT_WIDTH) * boardWidth
                    val nY = ((vertice.y as Double) / Const.BOARD_VIRT_HEIGHT) * boardHeight
                    return Vert(nX, nY)
                }
                val vertices = (body.vertices as Array<dynamic>).map { vert(it) }
                ctx.moveTo(vertices[0].x, vertices[0].y)
                vertices.forEach { ctx.lineTo(it.x, it.y) }
                ctx.lineTo(vertices[0].x, vertices[0].y)
            }

            ctx.lineWidth = 1.0
            ctx.strokeStyle = "black"
            ctx.stroke()
            ctx.translate(-offsetX, -offsetY)
        }

        val xtest = boardWidth * aX.coerceIn(0.0, 1.0)
        //ctx.drawImage(next.image, offsetX + xtest, offsetY, 32.0, 32.0)
        if(lastClick + timeout < Date.now())
            ctx.drawImage(next.image, offsetX + boardWidth + 128, window.innerHeight / 2.0 - 64, 128.0, 128.0)
        var n = 0.0
        Animal.values().forEachIndexed { i, animal ->
            var size = 96.0 * animal.size
            ctx.drawImage(animal.image, 32 + offsetX + boardWidth + n, 0.0, size, size)
            n += size
        }

        //Debug Text
        ctx.fillStyle = "black"
        ctx.font = "${size}px Roboto Mono"
        ctx.fillText("FPS: ${fpsCounter.getString()}", 0.0, size)
        val count = (Matter.Composite.allBodies(engine.world) as Array<dynamic>).size
        ctx.fillText("C: $aX", 0.0, size * 2)
        ctx.fillText("Bodies: $count", 0.0, size * 3)

        fpsCounter.frame()
    }

    var lastRender = 0.0
    private fun loop(timestamp: Double) {
        val delta = (timestamp - lastRender).coerceIn(0.0, 20.0)
        update(delta)
        draw()

        lastRender = timestamp
        window.requestAnimationFrame(::loop)
    }
}