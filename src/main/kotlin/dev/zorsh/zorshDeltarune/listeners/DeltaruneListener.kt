package dev.zorsh.zorshDeltarune.listeners

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInputEvent

class DeltaruneListener : Listener {

    @EventHandler
    fun onVehicleExitEvent(e: EntityDismountEvent) {
        val player = e.entity
        if (player is Player && e.dismounted is BlockDisplay && ZorshDeltarune.getDPlayer(player.uniqueId)?.locked == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInputEvent(e: PlayerInputEvent) {
        val player = e.player
        ZorshDeltarune.getDPlayer(player.uniqueId)?.updateInputs(e.input)
    }
}