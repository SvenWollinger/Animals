package io.wollinger.animals.utils

object Const {
    const val BOARD_VIRT_SCALE: Int = 1000
    const val BOARD_VIRT_DIFF = 0.8
    val BOARD_VIRT_HEIGHT = BOARD_VIRT_SCALE

    val BOARD_VIRT_WIDTH = (BOARD_VIRT_SCALE * BOARD_VIRT_DIFF).toInt()
    val ANIMAL_SCALE = BOARD_VIRT_WIDTH / 6

        const val BOARD_VIRT_WALL_THICKNESS = 32
}