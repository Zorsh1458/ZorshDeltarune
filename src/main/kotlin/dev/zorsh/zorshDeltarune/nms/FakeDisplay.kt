package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.utils.runLater
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

abstract class FakeDisplay(
    val entityId: Int,
    var transformation: Transformation,
    protected val teleportDuration: Int,
    protected val interpolationDuration: Int,
    var players: List<Player>,
    var holder: MutableSet<FakeDisplay>? = null,
) {

    var exists = true

    open fun destroy() {
        if (exists) {
            exists = false

            holder?.remove(this)
            repeat(10) { i ->
                runLater(i * 20L) {
                    PacketManager.removeEntity(entityId, players)
                }
            }
            runLater(400L) {
                PacketManager.removeEntity(entityId, players)
            }
        }
    }

    open fun destroy(player: Player) {
        players = players - player
        repeat(10) { i ->
            runLater(i * 20L) {
                PacketManager.removeEntity(entityId, listOf(player))
            }
        }
        runLater(400L) {
            PacketManager.removeEntity(entityId, listOf(player))
        }
    }

    open fun changeTransformation(
        newTransformation: Transformation,
        newText: Component = Component.text("___DEFAULT_TEXT___"),
        newOpacity: Byte = 255.toByte(),
    ) {
        PacketManager.setTransformation(entityId, newTransformation, players, interpolationDuration, teleportDuration)
        transformation = newTransformation
    }
}