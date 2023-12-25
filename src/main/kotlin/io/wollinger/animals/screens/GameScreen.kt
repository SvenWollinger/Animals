package io.wollinger.animals.screens

import io.wollinger.animals.Body
import io.wollinger.animals.Constants
import io.wollinger.animals.Matter
import io.wollinger.animals.Resources
import io.wollinger.animals.input.Button
import io.wollinger.animals.input.Input
import io.wollinger.animals.math.Rectangle
import io.wollinger.animals.math.Vector2
import io.wollinger.animals.utils.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlin.js.Date

class GameScreen: Screen {
    private val matter = Matter()
    private lateinit var next: Animal

    private fun newAnimal() {
        next = Animal.entries.toTypedArray().copyOfRange(0, 3).random()
    }

    init {
        newAnimal()
    }

    private val fpsCounter = FPSCounter()

    private fun addAnimal(animal: Animal, x: Double) = addAnimal(animal, x * Constants.BOARD_VIRT_WIDTH, 0.0)
    private fun addAnimal(animal: Animal, x: Double, y: Double, angle: Double = 0.0, velocity: Vector2 = Vector2()) {
        matter.addCircle(label = animal.name, x = x, y = y, radius = animal.size * Constants.ANIMAL_SCALE, angle = angle, velocity = velocity)
    }


    private var lastClick = Date.now()

    private val timeout = 500

    @OptIn(DelicateCoroutinesApi::class)
    fun won(winnerA: Body, winnerB: Body) {
        lastClick = Date.now() + 100_000_000
        matter.timescale = 0.0
        GlobalScope.launch {
            matter.getBodies().filter { it.label != Constants.WALL_ID && it.id != winnerA.id && it.id != winnerB.id }.forEach {
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
            matter.addCircle(Constants.COIN_ID, middle.x, middle.y, 1.0  * Constants.ANIMAL_SCALE)
            matter.timescale = 1.0
        }
    }

    init {

        matter.onCollisionStart { event  ->
            Resources.TOUCH.play()
            val blackList = ArrayList<Int>()
            event.pairs.filter { it.first.label != Constants.WALL_ID && it.second.label != Constants.WALL_ID }.forEach { pair ->
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
            matter.addRectangle(label = Constants.WALL_ID, isStatic = true, x = x + width / 2, y = y + height / 2, width = width, height = height)
        }
        val t = Constants.BOARD_VIRT_WALL_THICKNESS
        val w = Constants.BOARD_VIRT_WIDTH
        val h = Constants.BOARD_VIRT_HEIGHT
        wall(-t, 0, t, h)
        wall(w, 0, t, h)
        wall(0, h, w, t)

        val qs = localStorage.getItem(Constants.QUICKSAVE_ID)
        if(qs != null) load()
        launch {
            while(true) {
                delay(250)
                save()
            }
        }
    }

    @Serializable
    data class SavedAnimal(val animal: String, val position: Vector2, val angle: Double, val velocity: Vector2)
    @Serializable
    data class Save(val animals: List<SavedAnimal>)

    private fun saveString(): String {
        val animals = matter.getBodies().filter { it.label != Constants.WALL_ID && it.label != Constants.COIN_ID }.map {
            SavedAnimal(it.label, it.position, it.angle, it.velocity)
        }
        val save = Save(animals)
        return Json.encodeToString(save)
    }

    private fun save() {
        localStorage.setItem(Constants.QUICKSAVE_ID, saveString())
    }

    private fun load() {
        val json = localStorage.getItem(Constants.QUICKSAVE_ID) ?: return
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
            if(it.label == Constants.WALL_ID) return@forEach
            matter.remove(it)
        }
    }

    data class Cloud(val rect: Rectangle, val speed: Double, val image: Image = Resources.CLOUDS.random())
    private val clouds = ArrayList<Cloud>()
    private var cloudSpawn = 0.0
    private val cloudSpawnLimit: Double get() = (2500..15000).random().toDouble()

    private var frames = ArrayList<String>()

    private var maxFrame = 0

    private var buildInfo: BuildInfo? = null
    init {
        launch {
            buildInfo = dl<BuildInfo>("/build.json").await()
        }
    }

    val REWIND_FRAME_LIMIT = 5000
    val FRAME_REWIND_COUNT = 5

    override fun update(delta: Double, canvas: HTMLCanvasElement, input: Input) {
        if(lastClick + timeout < Date.now() && input.isPressed(Button.MOUSE_LEFT)) {
            val spawnX = ((input.mousePos.x - offset.x) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(next, spawnX)
            newAnimal()
            lastClick = Date.now()
        }

        if(input.isPressed("o")) {
            if(frames.size > FRAME_REWIND_COUNT) {
                repeat(FRAME_REWIND_COUNT - 1) { frames.removeLast() }
                loadString(frames.removeLast())
            }
        } else {
            frames.add(saveString())
            frames.limitFirst(REWIND_FRAME_LIMIT)
            maxFrame = frames.size
        }
        if(input.isJustPressed("s")) save()
        if(input.isJustPressed("r")) reset()
        if(input.isJustPressed("l")) load()

        matter.update(delta)
        if(input.isJustPressed("f")) isDebug = !isDebug

        if(clouds.size < 3 && cloudSpawn >= cloudSpawnLimit) {
            cloudSpawn = 0.0
            val heightZone = canvas.height / 5
            clouds.add(
                Cloud(
                    Rectangle(-199.0, (0..heightZone).random().toDouble(), 200.0, 200.0),
                    listOf(0.1, 0.05, 0.08).random()
                )
            )
        }

        clouds.forEach {
            it.rect.x +=  it.speed * delta
            val screen = Rectangle(0, 0, canvas.width, canvas.height)
            if(!screen.intersects(it.rect)) clouds.remove(it)
        }

        cloudSpawn += delta
    }

    private var tileSize = 0.0

    private var boardHeight = 0.0
    private var boardWidth = 0.0
    private var offset = Vector2()
    private var isDebug: Boolean
        get() = (localStorage.getItem("debug") ?: "false").toBoolean()
        set(value) = localStorage.setItem("debug", value.toString())

    override fun render(delta: Double, canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D) {
        var isMobile = false
        val width = canvas.width
        val height = canvas.height
        if(width >= height) {
            isMobile = false
            boardHeight = canvas.height * 0.9
            boardWidth = boardHeight * Constants.BOARD_VIRT_DIFF
            offset.x = (canvas.width / 2.0) - boardWidth / 2.0
            offset.y = 0.0
            tileSize = boardWidth / 16
        } else if(height > width) {
            isMobile = true
            boardWidth = canvas.width * 0.9
            boardHeight = boardWidth * 1.2
            offset.x = canvas.width * 0.05
            tileSize = boardWidth / 16
            offset.y = (canvas.height) - boardHeight - tileSize
        }

        if(canvas.width != canvas.width || canvas.height != canvas.height) {
            canvas.width = canvas.width
            canvas.height = canvas.height
            ctx.imageSmoothingEnabled = true
        }

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0, 0, canvas.width, canvas.height)

        clouds.forEach { it.rect.also { r -> ctx.drawImage(it.image, r.x, r.y, r.width, r.height) } }

        matter.getBodies().forEach {  body ->
            if(body.label == Constants.WALL_ID) return@forEach
            if(body.label == Constants.COIN_ID) {
                val x = ((body.position.x) / Constants.BOARD_VIRT_WIDTH) * boardWidth
                val y = ((body.position.y) / Constants.BOARD_VIRT_HEIGHT) * boardHeight
                ctx.use(
                    translateX = x + offset.x,
                    translateY = y + offset.y,
                    angle = body.angle
                ) {
                    val radius = ((body.circleRadius / Constants.BOARD_VIRT_WIDTH) * boardWidth)
                    drawImage(Resources.COIN, -radius, -radius, radius * 2, radius * 2)
                }
                return@forEach
            }
            val animal = Animal.valueOf(body.label)
            val x = ((body.position.x) / Constants.BOARD_VIRT_WIDTH) * boardWidth
            val y = ((body.position.y) / Constants.BOARD_VIRT_HEIGHT) * boardHeight
            ctx.use(
                translateX = x + offset.x,
                translateY = y + offset.y,
                angle = body.angle
            ) {
                val radius = ((body.circleRadius / Constants.BOARD_VIRT_WIDTH) * boardWidth)
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
                    val nX = (it.x / Constants.BOARD_VIRT_WIDTH) * boardWidth
                    val nY = (it.y / Constants.BOARD_VIRT_HEIGHT) * boardHeight
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
            val tileSize = canvas.width / (Animal.entries.size * 2.0 - 1)

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
                ctx.drawImage(next.image, offset.x + boardWidth + 128, canvas.height / 2.0 - 64, 128.0, 128.0)
            } else {
                val pSize = 128.0 * next.size
                val pureX = (offset.x + boardWidth / 2) - pSize / 2
                ctx.drawImage(next.image, pureX, 256.0, pSize, pSize)
            }
        }

        if(isDebug) {
            val textSize = canvas.height / 16.0
            var line = 1
            fun msg(message: String) {
                ctx.fillText(message, 0.0, textSize * line)
                line++
            }
            //Debug Text
            ctx.fillStyle = "black"
            ctx.font = "${textSize}px Roboto Mono"
            msg("FPS: ${fpsCounter.getString()}")
            msg("Bodies: ${matter.getBodies().size}")
            msg("Frames: ${frames.size}/$maxFrame")
            msg("Latest frame: ${frames.last()}")
            buildInfo?.let { msg("v${it.version} (${it.githash}) (${Date(it.timestamp).prettyString()}): ${it.commitMessage}") }
        }

        fpsCounter.frame()
    }

}