package dev.zorsh.zorshDeltarune.listeners

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerJoinEvent

class DeltaruneListener : Listener {

    @EventHandler
    fun onVehicleExitEvent(e: EntityDismountEvent) {
        val player = e.entity
        if (player is Player && e.dismounted is BlockDisplay && ZorshDeltarune.getDPlayer(player).locked) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerInputEvent(e: PlayerInputEvent) {
        val player = e.player
        ZorshDeltarune.getDPlayer(player).updateInputs(e.input)
    }

    @EventHandler
    fun onPlayerJoinEvent(e: PlayerJoinEvent) {
        val player = e.player
        if (ZorshDeltarune.deltarunePlayer[player] == null) {
            ZorshDeltarune.deltarunePlayer[player] = DeltarunePlayer(player)
        }
        ZorshDeltarune.deltarunePlayer[player]?.freeFromBattle()
    }
}