package io.wollinger.animals.utils

import io.wollinger.animals.Resources
import org.w3c.dom.Image

enum class Animal(val size: Double, val image: Image) {
    PARROT(0.2, Resources.PARROT),
    RABBIT(0.3, Resources.RABBIT),
    SNAKE(0.5, Resources.SNAKE),
    MONKEY(0.8, Resources.MONKEY),
    PIG(1.0, Resources.PIG),
    PENGUIN(1.15, Resources.PENGUIN),
    COW(1.35, Resources.COW),
    PANDA(1.5, Resources.PANDA),
    WALRUS(1.75, Resources.WALRUS),
    WHALE(2.0, Resources.WHALE);

    fun next(): Animal? = entries.getOrNull(entries.indexOf(this) + 1)
}