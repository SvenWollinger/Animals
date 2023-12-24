package io.wollinger.animals

object Constants {
    const val BOARD_VIRT_SCALE: Int = 1000
    const val BOARD_VIRT_DIFF = 0.8
    val BOARD_VIRT_HEIGHT = BOARD_VIRT_SCALE

    val BOARD_VIRT_WIDTH = (BOARD_VIRT_SCALE * BOARD_VIRT_DIFF).toInt()
    val ANIMAL_SCALE = BOARD_VIRT_WIDTH / 8

    const val BOARD_VIRT_WALL_THICKNESS = 256

    const val WALL_ID = "wall"
    const val COIN_ID = "coin"
    const val QUICKSAVE_ID = "quicksave"
}