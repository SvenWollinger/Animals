package io.wollinger.animals.screens

import io.wollinger.animals.Resources
import io.wollinger.animals.input.Button
import io.wollinger.animals.input.Input
import io.wollinger.animals.math.Rectangle
import io.wollinger.animals.utils.*
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlin.js.Date
import kotlin.math.min

class MenuScreen(private val screenManager: ScreenManager): Screen {
    enum class Page{ MAIN, ABOUT }
    private var page = Page.MAIN
    data class UIButton(val text: String, val images: Pair<Image, Image>, val rect: Rectangle = Rectangle(), var hover: Boolean = false, val action: () -> Unit)

    //global
    private val cover = UIButton("", Pair(Resources.COVER, Resources.COVER)) {  }
    private val backButton = UIButton("Back", Pair(Resources.BACK_BUTTON, Resources.BACK_BUTTON_H)) {
        when(page) {
            Page.ABOUT -> page = Page.MAIN
            else -> {}
        }
    }

    //main
    private val playButton = UIButton("Play", Pair(Resources.PLAY_BUTTON, Resources.PLAY_BUTTON_H)) { screenManager.screen = GameScreen() }
    private val aboutButton = UIButton("About", Pair(Resources.ABOUT_BUTTON, Resources.ABOUT_BUTTON_H)) { page = Page.ABOUT }

    //about
    private val aboutContent = Rectangle()
    private val kotlinLogo = UIButton("Kotlin", Pair(Resources.KOTLIN_LOGO, Resources.KOTLIN_LOGO)) { window.open("https://kotlinlang.org", "_blank") }
    private val matterLogo = UIButton("matterjs", Pair(Resources.MATTERJS_LOGO, Resources.MATTERJS_LOGO)) { window.open("https://brm.io/matter-js/", "_blank") }
    private val kenneyLogo = UIButton("kenney", Pair(Resources.KENNEY_LOGO, Resources.KENNEY_LOGO)) { window.open("https://www.kenney.nl", "_blank") }

    override fun update(delta: Double, canvas: HTMLCanvasElement, input: Input) {
        val useWidth = min(canvas.width, canvas.height)
        cover.rect.apply {
            width = useWidth / 1.5
            height = width / 2.0
            x = canvas.width / 2.0 - width / 2.0
            y = 0.0
        }
        canvas.title = ""

        val longDisplay = (canvas.height / canvas.width.toDouble()) >= 1.5

        aboutContent.apply {
            width = cover.rect.width / if(longDisplay) 1.25 else 2.0
            height = width
            x = canvas.width / 2 - aboutContent.width / 2
            y = cover.rect.height
        }
        kotlinLogo.apply {
            rect.y = aboutContent.y + aboutContent.height
            rect.x = aboutContent.x
            rect.width = aboutContent.width / 3
            rect.height = rect.width
            if(kotlinLogo.rect.contains(input.mousePos)) canvas.title = "Kotlin Logo (Click to open Website)"
        }
        matterLogo.apply {
            rect.y = aboutContent.y + aboutContent.height
            rect.x = aboutContent.x + kotlinLogo.rect.width
            rect.width = (aboutContent.width / 3) * 2
            rect.height = rect.width / 4
            if(matterLogo.rect.contains(input.mousePos)) canvas.title = "MatterJS Logo (Click to open Website)"
        }
        kenneyLogo.apply {
            rect.height = kotlinLogo.rect.height - matterLogo.rect.height
            rect.width = rect.height  * 2
            rect.y = aboutContent.y + aboutContent.height + matterLogo.rect.height
            rect.x = aboutContent.x + kotlinLogo.rect.width + rect.width / 2
            if(kenneyLogo.rect.contains(input.mousePos)) canvas.title = "Kenney's Assets (Click to open Website)"
        }
        backButton.apply {
            rect.width = aboutContent.width / 2
            rect.height = rect.width / 2
            rect.y = aboutContent.y + aboutContent.height + kotlinLogo.rect.height * 1.2
            rect.x = aboutContent.x + rect.width / 2
            hover = backButton.rect.contains(input.mousePos)
            if(hover) canvas.title = "Back button"
        }

        playButton.apply {
            rect.width = cover.rect.width / 2.0
            rect.height = playButton.rect.width / 2.0
            rect.x = canvas.width / 2 - playButton.rect.width / 2
            rect.y = cover.rect.height
            hover = playButton.rect.contains(input.mousePos)
        }

        aboutButton.apply {
            rect.width = cover.rect.width / 2.0
            rect.height = playButton.rect.width / 2.0
            rect.x = canvas.width / 2 - playButton.rect.width / 2
            rect.y = playButton.rect.y + playButton.rect.height + playButton.rect.height / 10
            hover = aboutButton.rect.contains(input.mousePos)
        }

        fun input(rect: Rectangle): Boolean {
            if(input.isPressed(Button.MOUSE_LEFT) && rect.contains(input.mousePos)) {
                input.clearPressed(Button.MOUSE_LEFT)
                return true
            }
            return false
        }

        fun input(button: UIButton) {
            if(input(button.rect)) button.action.invoke()
        }


        when(page) {
            Page.MAIN -> {
                input(playButton)
                input(aboutButton)
            }
            Page.ABOUT -> {
                input(kotlinLogo)
                input(matterLogo)
                input(kenneyLogo)
                input(backButton)
                if(input(vTextRect)) {
                    window.open("https://github.com/SvenWollinger/Animals/commit/$hash", "_blank")
                }
            }
        }
    }

    private fun drawButton(button: UIButton, ctx: CanvasRenderingContext2D) {
        val image = if(button.hover) button.images.second else button.images.first
        val r = button.rect
        ctx.drawImage(image, r.x, r.y, r.width, r.height)
    }

    private var hash: String = ""
    private var vText: String? = null
    private var vTextRect = Rectangle()
    init {
        launch {
            val b = dl<BuildInfo>("/build.json").await()
            hash = b.githash
            vText = "v${b.version} #${b.githash} ${Date(b.timestamp).prettyString()} - ${b.commitMessage}"
        }
    }

    override fun render(delta: Double, canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D) {
        drawButton(cover, ctx)
        when(page) {
            Page.MAIN -> {
                drawButton(playButton, ctx)
                drawButton(aboutButton, ctx)
            }
            Page.ABOUT -> {
                ctx.drawImage(Resources.ABOUT, aboutContent)
                drawButton(kotlinLogo, ctx)
                drawButton(matterLogo, ctx)
                drawButton(kenneyLogo, ctx)
                drawButton(backButton, ctx)
                vText?.let {
                    val fS = 20.0
                    ctx.fillStyle = "black"
                    ctx.font = "${fS}px Roboto Mono"
                    val textWidth = ctx.measureText(it).width
                    ctx.fillText(it, canvas.width / 2 - textWidth / 2, canvas.height - fS)
                    vTextRect.from(canvas.width / 2 - textWidth / 2, canvas.height - (fS * 2), textWidth, fS)
                }
            }
        }
    }
}