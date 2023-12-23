package io.wollinger.animals

import io.wollinger.animals.utils.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get
import kotlin.js.Date
import kotlin.js.json

@OptIn(DelicateCoroutinesApi::class)
class Engine(
    private val canvas: HTMLCanvasElement,
    private val ctx: CanvasRenderingContext2D,
    private val input: Input
) {
    private val matter = Matter()
    private lateinit var next: Animal

    private fun newAnimal() {
        next = Animal.entries.toTypedArray().copyOfRange(0, 3).random()
    }

    init {
        newAnimal()
    }

    private val fpsCounter = FPSCounter()

    private fun addAnimal(animal: Animal, x: Double) = addAnimal(animal, x * Const.BOARD_VIRT_WIDTH, 0.0)
    private fun addAnimal(animal: Animal, x: Double, y: Double, angle: Double = 0.0, velocity: Vector2 = Vector2()) {
        matter.addCircle(label = animal.name, x = x, y = y, radius = animal.size * Const.ANIMAL_SCALE, angle = angle, velocity = velocity)
    }

    private var isMobile = false

    private var lastClick = Date.now()

    private val timeout = 300

    @OptIn(DelicateCoroutinesApi::class)
    fun won(winnerA: Body, winnerB: Body) {
        lastClick = Date.now() + 100_000_000
        matter.timescale = 0.0
        GlobalScope.launch {
            matter.getBodies().filter { it.label != "wall" && it.id != winnerA.id && it.id != winnerB.id }.forEach {
                delay(200)
                matter.remove(it)
            }
            var running = true
            launch {
                delay(5000)
                running = false
            }
            var intense = 0.2
            launch {
                delay(25)
                intense += 1
            }
            while(running) {
                winnerA.ref.position.x += listOf(-intense, intense).random()
                winnerA.ref.position.y += listOf(-intense, intense).random()
                winnerB.ref.position.x += listOf(-intense, intense).random()
                winnerB.ref.position.y += listOf(-intense, intense).random()
                delay(50)
            }
            val bodyNewA = Body.fromDynamic(winnerA.ref)
            val bodyNewB = Body.fromDynamic(winnerB.ref)
            val middle = (bodyNewA.position + bodyNewB.position) / 2

            val directionA = (bodyNewA.position - middle) / 20
            val directionB = (bodyNewB.position - middle) / 20

            repeat(20) {
                winnerA.ref.position.x -= directionA.x + listOf(-intense, intense).random()
                winnerA.ref.position.y -= directionA.y + listOf(-intense, intense).random()
                winnerB.ref.position.x -= directionB.x + listOf(-intense, intense).random()
                winnerB.ref.position.y -= directionB.y + listOf(-intense, intense).random()
                delay(20)
            }
            repeat(20) {
                winnerA.ref.circleRadius += 10
                winnerB.ref.circleRadius += 10
                delay(1)
            }
            matter.remove(winnerA, winnerB)
            matter.addCircle("coin", middle.x, middle.y, 1.0  * Const.ANIMAL_SCALE)
            matter.timescale = 1.0
        }
    }

    init {
        window.addEventListener(type = "mousedown", options = false, callback = {
            if(lastClick + timeout > Date.now()) return@addEventListener
            it as MouseEvent
            val test = ((it.x - offset.x) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(next, test)
            newAnimal()
            lastClick = Date.now()
        })

        matter.onCollisionStart { event  ->
            Resources.TOUCH.play()
            val blackList = ArrayList<Int>()
            event.pairs.filter { it.first.label != "wall" && it.second.label != "wall" }.forEach { pair ->
                val bodyA = pair.first
                val bodyB = pair.second

                val animalA = Animal.valueOf(bodyA.label)
                val animalB = Animal.valueOf(bodyB.label)

                if(animalA == animalB && !blackList.containsAny(bodyA.id, bodyB.id)) {
                    blackList.addAll(bodyA.id, bodyB.id)
                    val middle = (bodyA.position + bodyB.position) / 2

                    val next = animalA.next()
                    if(next != null) {
                        Resources.POOF.play()
                        matter.remove(bodyA, bodyB)
                        addAnimal(next, middle.x, middle.y)
                    } else if(animalA == Animal.entries.last())
                        won(bodyA, bodyB)
                }
            }
        }

        fun wall(x: Int, y: Int, width: Int, height: Int) {
            matter.addRectangle(label = "wall", isStatic = true, x = x + width / 2, y = y + height / 2, width = width, height = height)
        }
        val t = Const.BOARD_VIRT_WALL_THICKNESS
        val w = Const.BOARD_VIRT_WIDTH
        val h = Const.BOARD_VIRT_HEIGHT
        wall(-t, 0, t, h)
        wall(w, 0, t, h)
        wall(0, h, w, t)

        val qs = localStorage.getItem("quicksave")
        if(qs != null) load()
        GlobalScope.launch {
            while(true) {
                delay(250)
                save()
            }
        }
        window.requestAnimationFrame(::loop)
    }

    @Serializable
    data class SavedAnimal(val animal: String, val position: Vector2, val angle: Double, val velocity: Vector2)
    @Serializable
    data class Save(val animals: List<SavedAnimal>)

    fun saveString(): String {
        val animals = matter.getBodies().filter { it.label != "wall" && it.label != "coin" }.map {
            SavedAnimal(it.label, it.position, it.angle, it.velocity)
        }
        val save = Save(animals)
        return Json.encodeToString(save)
    }

    private fun save() {
        localStorage.setItem("quicksave", saveString())
    }

    private fun load() {
        val json = localStorage.getItem("quicksave") ?: return
        loadString(json)
    }

    fun loadString(string: String) {
        reset()
        val save = Json.decodeFromString<Save>(string)
        matter.timescale = 0.0
        save.animals.forEach {
            val animal = Animal.valueOf(it.animal)
            addAnimal(animal, it.position.x, it.position.y, it.angle, it.velocity)
        }
        matter.timescale = 1.0
    }

    private fun reset() {
        matter.getBodies().forEach {
            if(it.label == "wall") return@forEach
            matter.remove(it)
        }
    }

    data class Cloud(val rect: Rectangle, val speed: Double, val image: Image = Resources.CLOUDS.random())
    private val clouds = ArrayList<Cloud>()
    private var cloudSpawn = 0.0
    private val cloudSpawnLimit: Double get() = (2500..15000).random().toDouble()

    var frames = ArrayList<String>()

    var maxFrame = 0
    private fun update(delta: Double) {
        if(input.isPressed("o")) {
            loadString(frames.removeLast())
        } else {
            frames.add(saveString())
            maxFrame = frames.size
        }
        if(input.isJustPressed("s")) save()
        if(input.isJustPressed("r")) reset()
        if(input.isJustPressed("l")) load()

        matter.update(delta)
        if(input.isJustPressed("f")) isDebug = !isDebug

        if(clouds.size < 3 && cloudSpawn >= cloudSpawnLimit) {
            cloudSpawn = 0.0
            val heightZone = window.innerHeight / 5
            clouds.add(Cloud(Rectangle(-199.0, (0..heightZone).random().toDouble(), 200.0, 200.0), listOf(0.1, 0.05, 0.08).random()))
        }

        clouds.forEach {
            it.rect.x +=  it.speed * delta
            val screen = Rectangle(0, 0, window.innerWidth, window.innerHeight)
            if(!screen.intersects(it.rect)) clouds.remove(it)
        }

        cloudSpawn += delta

    }

    private var buildInfo: BuildInfo? = null

    init {
        dl<BuildInfo>("/build.json") { buildInfo = it }
    }

    private var tileSize = 0.0

    private fun size() = canvas.height / 16.0
    private var boardHeight = 0.0
    private var boardWidth = 0.0
    private var offset = Vector2()
    private var isDebug = false
    private fun draw() {
        val width = window.innerWidth
        val height = window.innerHeight
        if(width >= height) {
            isMobile = false
            boardHeight = window.innerHeight * 0.9
            boardWidth = boardHeight * Const.BOARD_VIRT_DIFF
            offset.x = (window.innerWidth / 2.0) - boardWidth / 2.0
            offset.y = 0.0
            tileSize = boardWidth / 16
        } else if(height > width) {
            isMobile = true
            boardWidth = window.innerWidth * 0.9
            boardHeight = boardWidth * 1.2
            offset.x = window.innerWidth * 0.05
            tileSize = boardWidth / 16
            offset.y = (window.innerHeight) - boardHeight - tileSize
        }

        if(canvas.width != window.innerWidth || canvas.height != window.innerHeight) {
            canvas.width = window.innerWidth
            canvas.height = window.innerHeight
            ctx.imageSmoothingEnabled = true
        }
        val size = size()

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0, 0, window.innerWidth, window.innerHeight)

        clouds.forEach { it.rect.also { r -> ctx.drawImage(it.image, r.x, r.y, r.width, r.height) } }

        matter.getBodies().forEach {  body ->
            if(body.label == "wall") return@forEach
            if(body.label == "coin") {
                val x = ((body.position.x) / Const.BOARD_VIRT_WIDTH) * boardWidth
                val y = ((body.position.y) / Const.BOARD_VIRT_HEIGHT) * boardHeight
                ctx.use(
                    translateX = x + offset.x,
                    translateY = y + offset.y,
                    angle = body.angle
                ) {
                    val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth)
                    drawImage(Resources.COIN, -radius, -radius, radius * 2, radius * 2)
                }
                return@forEach
            }
            val animal = Animal.valueOf(body.label)
            val x = ((body.position.x) / Const.BOARD_VIRT_WIDTH) * boardWidth
            val y = ((body.position.y) / Const.BOARD_VIRT_HEIGHT) * boardHeight
            ctx.use(
                translateX = x + offset.x,
                translateY = y + offset.y,
                angle = body.angle
            ) {
                val radius = ((body.circleRadius / Const.BOARD_VIRT_WIDTH) * boardWidth)
                drawImage(animal.image, -radius, -radius, radius * 2, radius * 2)
            }
        }

        for(i in 0 until (boardHeight / tileSize).toInt() + 1) {
            ctx.drawImage(Resources.FENCE, offset.x - tileSize, offset.y + i * tileSize, tileSize, tileSize)
            ctx.drawImage(Resources.FENCE, offset.x + boardWidth, offset.y + i * tileSize, tileSize, tileSize)
        }

        for(i in -1 until (boardWidth / tileSize).toInt() + 1) {
            ctx.drawImage(Resources.GRASS, offset.x + i * tileSize, offset.y + boardHeight, tileSize, tileSize)
        }

        if(isDebug) {
            ctx.translate(offset.x, offset.y)
            ctx.fillStyle = "black"
            ctx.strokeStyle = "black"
            ctx.beginPath()

            matter.getBodies().forEach { body ->
                val vertices = body.vertices.map {
                    val nX = ((it.x) / Const.BOARD_VIRT_WIDTH) * boardWidth
                    val nY = ((it.y) / Const.BOARD_VIRT_HEIGHT) * boardHeight
                    Vector2(nX, nY)
                }
                ctx.trace(vertices)
            }

            ctx.lineWidth = 1.0
            ctx.strokeStyle = "black"
            ctx.stroke()
            ctx.translate(-offset.x, -offset.y)
        }

        if(!isMobile) {
            var n = 0.0
            Animal.entries.forEach { animal ->
                val currentSize = 96.0 * animal.size
                ctx.drawImage(animal.image, 32 + offset.x + boardWidth + n, 0.0, currentSize, currentSize)
                n += currentSize
            }
        } else {
            val tileSize = window.innerWidth / (Animal.entries.size * 2.0 - 1)

            var i = 0
            Animal.entries.forEach { animal ->
                ctx.drawImage(animal.image, i * tileSize, 0.0, tileSize, tileSize)
                i++
                if(animal != Animal.entries.last()) {
                    ctx.drawImage(Resources.ARROW_RIGHT, (i ) * tileSize, 0.0, tileSize, tileSize)
                    i++
                }
            }
        }

        if(lastClick + timeout < Date.now()) {
            if(!isMobile) {
                ctx.drawImage(next.image, offset.x + boardWidth + 128, window.innerHeight / 2.0 - 64, 128.0, 128.0)
            } else {
                val pSize = 128.0 * next.size
                val pureX = (offset.x + boardWidth / 2) - pSize / 2
                ctx.drawImage(next.image, pureX, 256.0, pSize, pSize)
            }
        }

        if(isDebug) {
            var line = 1
            fun msg(message: String) {
                ctx.fillText(message, 0.0, size * line)
                line++
            }
            //Debug Text
            ctx.fillStyle = "black"
            ctx.font = "${size}px Roboto Mono"
            msg("FPS: ${fpsCounter.getString()}")
            msg("Bodies: ${matter.getBodies().size}")
            msg("Frames: ${frames.size}/$maxFrame")
            msg("Latest frame: ${frames.last()}")
            buildInfo?.let { msg("v${it.version} (${it.githash}) (${Date(it.timestamp).prettyString()}): ${it.commitMessage}") }
        }

        fpsCounter.frame()
    }

    private var lastRender = 0.0
    private fun loop(timestamp: Double) {
        val delta = (timestamp - lastRender).coerceIn(0.0, 20.0)
        update(delta)
        draw()

        lastRender = timestamp
        window.requestAnimationFrame(::loop)
    }
}