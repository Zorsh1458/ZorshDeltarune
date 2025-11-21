package dev.zorsh.zorshDeltarune.bettermodel

import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.tracker.EntityHideOption
import kr.toxicity.model.api.tracker.EntityTracker
import net.kyori.adventure.text.Component
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
//            .map { r -> r.getOrCreate(ent, player).also { tracker ->
//                tracker.h
//            } }
                .map { r -> r.getOrCreate(anchor, player) }
                .orElse(null)
            Bukkit.getOnlinePlayers().forEach { pl ->
                if (pl.uniqueId != player.uniqueId) {
                    tracker.hide(pl)
                    Bukkit.broadcast(Component.text("Hiding ${player.name}'s model from ${pl.name}"))
                }
            }
            tracker.displays().forEach { display ->
                display.brightness(15, 15)
                trackedModels += display.id()
            }
            runLater(10) {
                tracker.animate("idle")
            }
        } catch (ignored: Exception) {}
    }

    fun remove() {
        runLater(0) {
            tracker.despawn()
            anchor.remove()
        }
    }
}