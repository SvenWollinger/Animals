package io.wollinger.animals

import io.wollinger.animals.utils.Vector2
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

enum class Button(val id: Int) {
    MOUSE_LEFT(0), MOUSE_MIDDLE(1), MOUSE_RIGHT(2)
}

class Input {
    private val pressed = HashSet<String>()
    var mousePos = Vector2()
    private var buttons = HashSet<Button>()


    fun handle(event: Event) {
        when(event) {
            is KeyboardEvent -> {
                when(event.type) {
                    "keydown" -> pressed.add(event.key)
                    "keyup" -> pressed.remove(event.key)
                }
            }
            is MouseEvent -> {
                mousePos.x = event.x
                mousePos.y = event.y
                when(event.type) {
                    "mousedown" -> buttons.add(Button.entries.first { it.id == event.button.toInt() })
                    "mouseup" -> buttons.remove(Button.entries.first { it.id == event.button.toInt() })
                }
            }
        }
    }

    fun isPressed(button: Button) = buttons.contains(button)

    fun isPressed(key: String) = pressed.contains(key)
    fun isJustPressed(key: String): Boolean {
        if(isPressed(key)) {
            pressed.remove(key)
            return true
        }
        return false
    }
}