package io.wollinger.animals

import org.w3c.dom.events.KeyboardEvent

class Input {
    private val pressed = HashSet<String>()
    fun handle(event: KeyboardEvent) {
        when(event.type) {
            "keydown" -> pressed.add(event.key)
            "keyup" -> pressed.remove(event.key)
        }
    }

    fun isPressed(key: String) = pressed.contains(key)
    fun isJustPressed(key: String): Boolean {
        if(isPressed(key)) {
            pressed.remove(key)
            return true
        }
        return false
    }
}