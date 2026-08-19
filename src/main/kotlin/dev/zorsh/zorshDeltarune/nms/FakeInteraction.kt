package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.utils.runLater
import org.bukkit.Location
import org.bukkit.entity.Player

class FakeInteraction(
    val entityId: Int,
    val width: Float,
    val height: Float,
    val location: Location,
    val players: List<Player>
) {

    var exists = true

    fun destroy() {
        if (exists) {
            exists = false
            repeat(5) { i ->
                runLater(i * 20L) {
                    PacketManager.removeEntity(entityId, players)
                }
            }
            runLater(400L) {
                PacketManager.removeEntity(entityId, players)
            }
        }
    }
}