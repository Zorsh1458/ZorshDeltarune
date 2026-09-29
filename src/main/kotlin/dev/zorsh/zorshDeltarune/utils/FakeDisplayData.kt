package dev.zorsh.zorshDeltarune.utils

import org.bukkit.util.Transformation

data class FakeDisplayData(
    val transformation: Transformation,
    val opacity: Byte = 255.toByte(),
    val teleportDuration: Int = 2,
    val interpolationDuration: Int = 2
)