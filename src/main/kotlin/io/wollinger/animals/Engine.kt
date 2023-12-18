package io.wollinger.animals

import io.wollinger.animals.utils.FPSCounter
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

data class AnimalInstance(
    val animal: Animal
)

object Const {
    const val BOARD_VIRT_SCALE: Int = 1000
    const val BOARD_VIRT_DIFF = 0.8
    val BOARD_VIRT_HEIGHT = BOARD_VIRT_SCALE

    val BOARD_VIRT_WIDTH = (BOARD_VIRT_SCALE * BOARD_VIRT_DIFF).toInt()
    const val BOARD_VIRT_WALL_THICKNESS = 32
}

class Engine(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D,
    private val input: Input
) {
    private val animals = HashMap<Int, AnimalInstance>()
    private val engine = Matter.Engine.create()

    private val fpsCounter = FPSCounter()

    fun addAnimal(animal: Animal, x: Double) {
        val body = Matter.Bodies.circle(x * Const.BOARD_VIRT_WIDTH, 0, (15..100).random(), { }, 25)
        Matter.Composite.add(engine.world, arrayOf(body))
        animals[body.id as Int] = AnimalInstance(animal)
    }


    init {

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
            addAnimal(AnimalDB[0], (1..10).random() / 10.0)
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
            ctx.translate(x + offsetX , y + offsetY);
            ctx.rotate(body.angle as Double)
            val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth) as Double
            ctx.drawImage(animal.animal.image, -radius, -radius, radius * 2, radius * 2)
            ctx.rotate(-(body.angle as Double))
            ctx.translate(-(x + offsetX), -(y + offsetY));
        }

        //Debug Text
        ctx.fillStyle = "black"
        ctx.font = "${size}px Roboto Mono"
        ctx.fillText("FPS: ${fpsCounter.getString()}", 0.0, size)

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