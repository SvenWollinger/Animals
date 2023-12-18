package io.wollinger.animals

import io.wollinger.animals.utils.Const
import io.wollinger.animals.utils.FPSCounter
import io.wollinger.animals.utils.use
import kotlinx.browser.window
import kotlinx.html.MATH
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.MouseEvent
import kotlin.math.min


class Engine(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D,
    private val input: Input
) {
    private val animals = HashMap<Int, Animal>()
    private val engine = Matter.Engine.create()

    private val fpsCounter = FPSCounter()

    fun addAnimal(animal: Animal, x: Double) {
        val body = Matter.Bodies.circle(x * Const.BOARD_VIRT_WIDTH, 0, animal.size * Const.ANIMAL_SCALE, { }, 25)
        Matter.Composite.add(engine.world, arrayOf(body))
        animals[body.id as Int] = animal
    }

    var aX = 0.0

    init {

        window.addEventListener(type = "mousedown", options = false, callback = {
            it as MouseEvent
            val boardHeight = window.innerHeight * 0.8
            val boardWidth = boardHeight * Const.BOARD_VIRT_DIFF
            val test = ((it.x - 64) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(Animal.values().random(), test)
        })

        Matter.Events.on(engine, "collisionActive") { event  ->
            (event.pairs as Array<dynamic>).forEach {
                println("id: ${it.id}")
                val coll = animals[it.id]
                println("Collided with: $coll")
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




    var counter = 0.0
    private fun update(delta: Double) {
        counter += delta
        if(counter >= 1000) {
            counter -= 1000
            //addAnimal(AnimalDB[0], (1..10).random() / 10.0)
        }

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
        //Size of one sprite

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0.0, 0.0, window.innerWidth.toDouble(), window.innerHeight.toDouble())

        val boardHeight = window.innerHeight * 0.8
        val boardWidth = boardHeight * Const.BOARD_VIRT_DIFF

        val offsetX = 64.0
        val offsetY = 64.0

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

            ctx.lineWidth = 1.0;
            ctx.strokeStyle = "black";
            ctx.stroke()
            ctx.translate(-offsetX, -offsetY)
        }

        (Matter.Composite.allBodies(engine.world) as Array<dynamic>).forEach { body ->
            val animal = animals[body.id as Int] ?: return@forEach

            val x = ((body.position.x as Double) / Const.BOARD_VIRT_WIDTH) * boardWidth
            val y = ((body.position.y as Double) / Const.BOARD_VIRT_HEIGHT) * boardHeight
            ctx.use(
                translateX = x + offsetX,
                translateY = y + offsetY,
                angle = body.angle as Double
            ) {
                val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth) as Double
                drawImage(animal.image, -radius, -radius, radius * 2, radius * 2)
            }

        }

        //ctx.drawImage(AnimalDB[1].image, offsetX, offsetY, 32.0, 32.0)

        //Debug Text
        ctx.fillStyle = "black"
        ctx.font = "${size}px Roboto Mono"
        ctx.fillText("FPS: ${fpsCounter.getString()}", 0.0, size)
        ctx.fillText("C: $aX", 0.0, size * 2)


        fpsCounter.frame()
    }

    var lastRender = 0.0
    private fun loop(timestamp: Double) {
        val delta = (timestamp - lastRender)
        update(delta)
        draw()

        lastRender = timestamp
        window.requestAnimationFrame(::loop)
    }
}