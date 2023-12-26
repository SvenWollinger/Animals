package io.wollinger.animals.screens

import io.wollinger.animals.*
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

    private fun addAnimal(animal: Animal, x: Double) = addAnimal(animal, x * Constants.BOARD_VIRT_WIDTH,  -Constants.ANIMAL_SCALE.toDouble())
    private fun addAnimal(animal: Animal, x: Double, y: Double, angle: Double = 0.0, velocity: Vector2 = Vector2()) {
        matter.addCircle(label = animal.name, x = x, y = y, radius = animal.size * Constants.ANIMAL_SCALE, angle = angle, velocity = velocity)
    }


    private var lastClick = Date.now()

    private val timeout = 500

    private var isLoosing = false
    private fun loose() {
        if(isLoosing) return
        launch {
            isLoosing = true
            lastClick = Date.now() + 100_000_000
            matter.timescale = 0.0
            repeat(50) {
                matter.getBodies().forEach {
                    it.ref.position.x += listOf(-1.0, 1.0).random()
                    it.ref.position.y += listOf(-1.0, 1.0).random()
                }
                delay(50)
            }
            matter.getBodies().filter { !isWhitelist(it.label) }.forEach {
                delay(200)
                matter.remove(it)
            }
            matter.timescale = 1.0 //TODO: Somethings wrong here.. everythings.. weird
            lastClick = Date.now()
            isLoosing = false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun won(winnerA: Body, winnerB: Body) {
        lastClick = Date.now() + 100_000_000
        matter.timescale = 0.0
        GlobalScope.launch {
            matter.getBodies().filter { !isWhitelist(it.label) && it.id != winnerA.id && it.id != winnerB.id }.forEach {
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
            event.pairs.filter { it.first.label == Constants.DEATH_TRIGGER_ID || it.second.label == Constants.DEATH_TRIGGER_ID }.forEach { pair ->
                if(!isAnimal(pair.first.label) && !isAnimal(pair.second.label)) return@forEach
                launch {
                    delay(4000)
                    if(matter.collided(pair.first, pair.second)) loose()
                }
            }

            var played = false
            val blackList = ArrayList<Int>()
            event.pairs.filter { !isWhitelist(it.first.label) && !isWhitelist(it.second.label) }.forEach { pair ->
                if(!played) {
                    Resources.TOUCH.play()
                    played = true
                }
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

        fun wall(x: Int, y: Int, width: Int, height: Int, trigger: Boolean = false, label: String = Constants.WALL_ID) {
            matter.addRectangle(label = label, isStatic = true, x = x + width / 2, y = y + height / 2, width = width, height = height, isSensor = trigger)
        }
        val t = Constants.BOARD_VIRT_WALL_THICKNESS
        val w = Constants.BOARD_VIRT_WIDTH
        val h = Constants.BOARD_VIRT_HEIGHT
        wall(0, -t, w, t, true, "death_trigger")
        wall(-t, -t, t, h + t)
        wall(w, -t, t, h + t)
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
        val animals = matter.getBodies().filter { !isWhitelist(it.label) }.map {
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
            if(isResetSafe(it.label)) return@forEach
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

    private val REWIND_FRAME_LIMIT = 5000
    private val FRAME_REWIND_COUNT = 5

    override fun update(delta: Double, canvas: HTMLCanvasElement, input: Input) {
        //Debug box
        if(input.isJustPressed("j") && isDebug) {
            val spawnX = ((input.mousePos.x - offset.x) / boardWidth) * Constants.BOARD_VIRT_WIDTH
            val spawnY = ((input.mousePos.y - offset.y) / boardHeight) * Constants.BOARD_VIRT_HEIGHT
            val size = Constants.BOARD_VIRT_WIDTH / 5
            matter.addRectangle(label = Constants.DEBUG_OBJECT_ID, x = spawnX.toInt(), y = spawnY.toInt(), size * 2, size)
        }

        //Spawn animal + timer function
        if(lastClick + timeout < Date.now() && input.isPressed(Button.MOUSE_LEFT)) {
            val spawnX = ((input.mousePos.x - offset.x) / boardWidth).coerceIn(0.0, 1.0)
            addAnimal(next, spawnX)
            newAnimal()
            lastClick = Date.now()
        }

        //Rewind test code, might become an ability in the future
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

        //Hotkeys
        if(input.isJustPressed("s")) save()
        if(input.isJustPressed("r")) reset()
        if(input.isJustPressed("l")) load()
        if(input.isJustPressed("f")) isDebug = !isDebug

        //Update physics
        matter.update(delta)

        //Cloud logic
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
            boardHeight = canvas.height * 0.8
            boardWidth = boardHeight * Constants.BOARD_VIRT_DIFF
            offset.x = (canvas.width / 2.0) - boardWidth / 2.0
            tileSize = boardWidth / 16
            offset.y = canvas.height - boardHeight - tileSize
        } else if(height > width) {
            isMobile = true
            boardWidth = canvas.width * 0.9
            boardHeight = boardWidth * 1.2
            offset.x = canvas.width * 0.05
            tileSize = boardWidth / 16
            offset.y = (canvas.height) - boardHeight - tileSize
        }

        //Fill background
        ctx.fillStyle = "#9290ff"
        ctx.fillRect(0, 0, canvas.width, canvas.height)

        //Render clouds
        clouds.forEach { it.rect.also { r -> ctx.drawImage(it.image, r.x, r.y, r.width, r.height) } }

        //Render bodies
        matter.getBodies().forEach {  body ->
            //Coin
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

            //Debug Box
            if(body.label == Constants.DEBUG_OBJECT_ID) {
                val x = ((body.position.x) / Constants.BOARD_VIRT_WIDTH) * boardWidth
                val y = ((body.position.y) / Constants.BOARD_VIRT_HEIGHT) * boardHeight
                ctx.use(
                    translateX = x + offset.x,
                    translateY = y + offset.y,
                    angle = body.angle
                ) {
                    val boxWidth = ((body.ref.width as Double / Constants.BOARD_VIRT_WIDTH) * boardWidth)
                    val boxHeight = ((body.ref.height as Double / Constants.BOARD_VIRT_HEIGHT) * boardHeight)

                    drawImage(Resources.WOOD_CRATE, -(boxWidth / 2), -(boxHeight / 2), boxWidth, boxHeight)
                }
                return@forEach
            }

            if(!isAnimal(body.label)) return@forEach
            //Render our funny little animals
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

        //Draw walls
        for(i in 0 until (boardHeight / tileSize).toInt() + 1) {
            ctx.drawImage(Resources.FENCE, offset.x - tileSize, offset.y + i * tileSize, tileSize, tileSize)
            ctx.drawImage(Resources.FENCE, offset.x + boardWidth, offset.y + i * tileSize, tileSize, tileSize)
        }

        //Draw floor
        for(i in -1 until (boardWidth / tileSize).toInt() + 1) {
            ctx.drawImage(Resources.GRASS, offset.x + i * tileSize, offset.y + boardHeight, tileSize, tileSize)
        }

        //Draw collision outlines
        if(isDebug) {
            ctx.translate(offset.x, offset.y)


            matter.getBodies().forEach { body ->
                fun a(opacity: String) = when(body.label) {
                    Constants.DEATH_TRIGGER_ID -> "rgba(255, 0, 0, $opacity)"
                    Constants.WALL_ID -> "rgba(0, 0, 255, $opacity)"
                    Constants.DEBUG_OBJECT_ID -> "rgba(255, 255, 255, $opacity)"
                    else -> "rgba(0, 255, 0, $opacity)"
                }
                ctx.fillStyle = a("0.25")
                ctx.strokeStyle = a("1.0")
                ctx.beginPath()
                val vertices = body.vertices.map {
                    val nX = (it.x / Constants.BOARD_VIRT_WIDTH) * boardWidth
                    val nY = (it.y / Constants.BOARD_VIRT_HEIGHT) * boardHeight
                    Vector2(nX, nY)
                }
                ctx.trace(vertices)
                ctx.lineWidth = 2.0
                ctx.fill()
                ctx.stroke()
            }


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
            msg("Timescale: ${matter.timescale}")
            buildInfo?.let { msg("v${it.version} (${it.githash}) (${Date(it.timestamp).prettyString()}): ${it.commitMessage}") }
        }

        fpsCounter.frame()
    }

}