package io.wollinger.animals

import io.wollinger.animals.utils.Animal

object Constants {
    const val BOARD_VIRT_SCALE: Int = 1000
    const val BOARD_VIRT_DIFF = 0.8
    val BOARD_VIRT_HEIGHT = BOARD_VIRT_SCALE

    val BOARD_VIRT_WIDTH = (BOARD_VIRT_SCALE * BOARD_VIRT_DIFF).toInt()
    val ANIMAL_SCALE = BOARD_VIRT_WIDTH / 8

    const val BOARD_VIRT_WALL_THICKNESS = 256

    const val QUICKSAVE_ID = "quicksave"
    //Objects
    const val WALL_ID = "wall"
    const val COIN_ID = "coin"
    const val DEATH_TRIGGER_ID = "death_trigger"
    const val DEBUG_OBJECT_ID = "debug_obj"

    val WHITELIST_IDS = listOf(WALL_ID, COIN_ID, QUICKSAVE_ID, DEATH_TRIGGER_ID, DEBUG_OBJECT_ID)
    val RESET_SAFE_IDS = listOf(WALL_ID, DEATH_TRIGGER_ID)
}

fun isResetSafe(label: String) = Constants.RESET_SAFE_IDS.contains(label)
fun isWhitelist(label: String) = Constants.WHITELIST_IDS.contains(label)
fun isAnimal(label: String) = Animal.entries.map { it.name }.contains(label)