package dev.zorsh.zorshDeltarune.listeners

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.PacketManager
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerQuitEvent

class DeltaruneListener : Listener {

    @EventHandler
    fun onEntityDamageEvent(e: EntityDamageEvent) {
        val player = e.entity
        if (player is Player) {
            if (ZorshDeltarune.getDPlayer(player.uniqueId)?.locked == true) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerDropItemEvent(e: PlayerDropItemEvent) {
        val player = e.player
        if (ZorshDeltarune.getDPlayer(player.uniqueId)?.locked == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDropItemEvent(e: BlockPlaceEvent) {
        val player = e.player
        if (ZorshDeltarune.getDPlayer(player.uniqueId)?.locked == true) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDropItemEvent(e: BlockBreakEvent) {
        val player = e.player
        if (ZorshDeltarune.getDPlayer(player.uniqueId)?.locked == true) {
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