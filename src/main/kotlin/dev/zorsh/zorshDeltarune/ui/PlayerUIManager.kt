package dev.zorsh.zorshDeltarune.ui

import org.bukkit.entity.Player
import java.util.UUID

@Deprecated("Old versioned class")
class PlayerUIManager {
    val playerCanvas = mutableMapOf<UUID, PlayerUICanvas>()

    fun initCanvas(pl: Player) {
        val cv = PlayerUICanvas()
        cv.initialize(listOf(pl))
        playerCanvas[pl.uniqueId] = cv
    }

    fun getCanvas(pl: Player): PlayerUICanvas? = playerCanvas[pl.uniqueId]
}