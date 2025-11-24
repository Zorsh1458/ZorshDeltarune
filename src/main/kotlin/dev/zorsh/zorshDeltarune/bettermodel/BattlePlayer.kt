package dev.zorsh.zorshDeltarune.bettermodel

import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.runLater
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.tracker.EntityTracker
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class BattlePlayer {

    companion object {
        val trackedModels = mutableListOf<Int>()
    }

    private lateinit var anchor: Entity
    private lateinit var tracker: EntityTracker

    fun create(player: Player, location: Location) {
        try {
            anchor = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY)
            anchor.isPersistent = false

            tracker = BetterModel.limb("battleplayer")
                .map { r -> r.getOrCreate(anchor, player) }
                .orElse(null)

            tracker.displays().forEach { display ->
                PacketManager.removeEntity(
                    display.id(),
                    Bukkit.getOnlinePlayers().filter { pl ->
                        pl.uniqueId != player.uniqueId
                    }
                )
                display.brightness(15, 15)
                trackedModels += display.id()
                PacketManager.privateEntities[display.id()] = setOf(player)
            }

            runLater(10) {
                tracker.animate("idle")
            }
        } catch (_: Exception) {}
    }

    fun remove() {
        runLater(0) {
            tracker.displays().forEach { display ->
                PacketManager.privateEntities.remove(display.id())
            }
            tracker.despawn()
            anchor.remove()
        }
    }
}