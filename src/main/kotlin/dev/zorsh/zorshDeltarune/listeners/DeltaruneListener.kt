package dev.zorsh.zorshDeltarune.listeners

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.PacketManager
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerQuitEvent

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

    @EventHandler
    fun onPlayerQuitEvent(e: PlayerQuitEvent) {
        val id = e.player.uniqueId
        if (PacketManager.lockedTimeTracker.contains(id)) {
            PacketManager.lockedTimeTracker.remove(id)
        }
    }
}