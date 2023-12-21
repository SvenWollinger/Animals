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
    private val matter = Matter()
    private lateinit var next: Animal

    private fun newAnimal() {
        next = Animal.values().copyOfRange(0, 3).random()
    }

    init {
        newAnimal()
    }

    private val fpsCounter = FPSCounter()

    private fun addAnimal(animal: Animal, x: Double) = addAnimal(animal, x * Const.BOARD_VIRT_WIDTH, 0.0)
    private fun addAnimal(animal: Animal, x: Double, y: Double) {
        matter.addCircle(label = animal.name, x = x, y = y, radius = animal.size * Const.ANIMAL_SCALE)
    }

    var isMobile = false

    private var aX = 0.0
    private var lastClick = Date.now()

    private val timeout = 300
    init {
        window.addEventListener(type = "mousedown", options = false, callback = {
            if(lastClick + timeout > Date.now()) return@addEventListener
            it as MouseEvent
            val test = ((it.x - offsetX) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(next, test)
            newAnimal()
            lastClick = Date.now()
        })



        matter.onCollisionStart { event  ->
            event.pairs.forEach { pair ->
                val bodyA = pair.first
                val bodyB = pair.second
                if(bodyA.label == "wall" || bodyB.label == "wall") return@forEach

                val animalA = Animal.valueOf(bodyA.label)
                val animalB = Animal.valueOf(bodyB.label)

                if(animalA == animalB) {
                    val middle = (bodyA.position + bodyB.position) / 2

                    matter.remove(bodyA.ref, bodyB.ref)

                    val next = Animal.values().indexOf(animalA) + 1
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
            aX = (it.x - offsetX) / boardWidth
        })

        fun wall(x: Int, y: Int, width: Int, height: Int) {
            matter.addRectangle(label = "wall", isStatic = true, x = x + width / 2, y = y + height / 2, width = width, height = height)
        }
        val t = Const.BOARD_VIRT_WALL_THICKNESS
        val w = Const.BOARD_VIRT_WIDTH
        val h = Const.BOARD_VIRT_HEIGHT
        wall(-t, 0, t, h)
        wall(w, 0, t, h)
        wall(0, h, w, t)

        window.requestAnimationFrame(::loop)
    }

    private fun update(delta: Double) {
        matter.update(delta)
        if(input.isJustPressed("f")) isDebug = !isDebug
    }

    private val iFenceMid = image("/img/ladder_mid.png")
    private val iGrass = image("/img/grass.png")
    var tileSize = 0.0


    private fun size() = canvas.height / 16.0
    var boardHeight: Double = 0.0// =
    var boardWidth: Double = 0.0// = boardHeight * Const.BOARD_VIRT_DIFF
    var offsetX: Double = 0.0
    var offsetY = 0.0
    var isDebug = false
    private fun draw() {
        val width = window.innerWidth
        val height = window.innerHeight
        if(width >= height) {
            isMobile = false
            boardHeight = window.innerHeight * 0.9
            boardWidth = boardHeight * Const.BOARD_VIRT_DIFF
            offsetX = (window.innerWidth / 2.0) - boardWidth / 2.0
            offsetY = 0.0
            tileSize = boardWidth / 16
        } else if(height > width) {
            isMobile = true
            boardWidth = window.innerWidth * 0.9
            boardHeight = boardWidth * 1.2
            offsetX = window.innerWidth * 0.05
            tileSize = boardWidth / 16
            offsetY = (window.innerHeight) - boardHeight - tileSize
        }

        if(canvas.width != window.innerWidth || canvas.height != window.innerHeight) {
            canvas.width = window.innerWidth
            canvas.height = window.innerHeight
            ctx.imageSmoothingEnabled = false
        }
        val size = size()

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0.0, 0.0, window.innerWidth.toDouble(), window.innerHeight.toDouble())


        matter.getBodies().forEach {  body ->
            if(body.label == "wall") return@forEach
            val animal = Animal.valueOf(body.label)
            val x = ((body.position.x) / Const.BOARD_VIRT_WIDTH) * boardWidth
            val y = ((body.position.y) / Const.BOARD_VIRT_HEIGHT) * boardHeight
            ctx.use(
                translateX = x + offsetX,
                translateY = y + offsetY,
                angle = body.angle
            ) {
                val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth)
                drawImage(animal.image, -radius, -radius, radius * 2, radius * 2)
            }
        }

        if(isDebug) {
            ctx.translate(offsetX, offsetY)
            ctx.fillStyle = "black"
            ctx.strokeStyle = "black"
            ctx.beginPath()

            matter.getBodies().forEach { body ->
                data class Vert(val x: Double, val y: Double)
                fun vert(vertice: dynamic): Vert {
                    val nX = ((vertice.x as Double) / Const.BOARD_VIRT_WIDTH) * boardWidth
                    val nY = ((vertice.y as Double) / Const.BOARD_VIRT_HEIGHT) * boardHeight
                    return Vert(nX, nY)
                }
                val vertices = body.vertices.map { vert(it) }
                ctx.moveTo(vertices[0].x, vertices[0].y)
                vertices.forEach { ctx.lineTo(it.x, it.y) }
                ctx.lineTo(vertices[0].x, vertices[0].y)
            }

            ctx.lineWidth = 1.0
            ctx.strokeStyle = "black"
            ctx.stroke()
            ctx.translate(-offsetX, -offsetY)
        }

        //val xtest = boardWidth * aX.coerceIn(0.0, 1.0)
        //ctx.drawImage(next.image, offsetX + xtest, offsetY, 32.0, 32.0)
        if(lastClick + timeout < Date.now()) {
            if(!isMobile) {
                var n = 0.0
                Animal.values().forEach { animal ->
                    val currentSize = 96.0 * animal.size
                    ctx.drawImage(animal.image, 32 + offsetX + boardWidth + n, 0.0, currentSize, currentSize)
                    n += currentSize
                }
                ctx.drawImage(next.image, offsetX + boardWidth + 128, window.innerHeight / 2.0 - 64, 128.0, 128.0)
            } else {
                val pSize = 128.0 * next.size
                val pureX = (offsetX + boardWidth / 2) - pSize / 2
                ctx.drawImage(next.image, pureX, 256.0, pSize, pSize)
                var tileGG = window.innerWidth / Animal.values().size + 0.0
                Animal.values().forEachIndexed { i, animal ->
                    val pX = i * tileGG
                    ctx.drawImage(animal.image, pX.toDouble(), 0.0, tileGG, tileGG)
                }
            }
        }



        val cY = (boardHeight / tileSize).toInt() + 1
        for(i in 0 until cY) {
            ctx.drawImage(iFenceMid, offsetX - tileSize, offsetY + i * tileSize, tileSize, tileSize)
            ctx.drawImage(iFenceMid, offsetX + boardWidth, offsetY + i * tileSize, tileSize, tileSize)

        }

        val c = (boardWidth / tileSize).toInt() + 1
        for(i in -1 until c) {
            ctx.drawImage(iGrass, offsetX + i * tileSize, offsetY + boardHeight, tileSize, tileSize)
        }

        if(isDebug) {
            //Debug Text
            ctx.fillStyle = "black"
            ctx.font = "${size}px Roboto Mono"
            ctx.fillText("FPS: ${fpsCounter.getString()}", 0.0, size)
            val count = matter.getBodies().size
            ctx.fillText("C: $aX", 0.0, size * 2)
            ctx.fillText("Bodies: $count", 0.0, size * 3)
        }

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