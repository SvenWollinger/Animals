package io.wollinger.animals.utils

import kotlinx.serialization.Serializable

@Serializable
data class BuildInfo(
    val version: String,
    val githash: String,
    val commitMessage: String,
    val timestamp: Long
)