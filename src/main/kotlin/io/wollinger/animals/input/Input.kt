package io.wollinger.animals.input

import io.wollinger.animals.math.Vector2
import io.wollinger.animals.utils.containsAny
import kotlinx.browser.window
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get

class Input {
    private val pressed = HashSet<String>()
    private val checked = HashSet<String>()
    var mousePos = Vector2()
    private var buttons = HashSet<Button>()

    fun handle(event: Event) {
        //Firefox does not support the touch event on devices without touch
        val hasTouch = window.asDynamic().TouchEvent != undefined
        if(hasTouch && event is TouchEvent) {
            event.touches[0]?.let { mousePos.set(x = it.clientX, y = it.clientY) }
            when(event.type) {
                "touchstart" -> buttons.add(Button.MOUSE_LEFT)
                "touchend" -> buttons.remove(Button.MOUSE_LEFT)
            }
            return
        }

        when(event) {
            is KeyboardEvent -> {
                when(event.type) {
                    "keydown" -> pressed.add(event.key)
                    "keyup" -> {
                        pressed.remove(event.key)
                        checked.remove(event.key)
                    }
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
    fun clearPressed(button: Button) = buttons.remove(button)

    fun isPressed(key: String) = pressed.contains(key)
    fun isJustPressed(key: String): Boolean {
        if(isPressed(key) && !checked.containsAny(key)) {
            pressed.remove(key)
            checked.add(key)
            return true
        }
        return false
    }
}