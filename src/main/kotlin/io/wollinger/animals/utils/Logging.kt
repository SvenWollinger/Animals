package io.wollinger.animals.utils

fun error(message: Any) = console.error(message)
fun info(message: Any) = console.info(message)
fun warn(message: Any) = console.warn(message)
fun debug(message: Any) = console.info("[debug] $message")