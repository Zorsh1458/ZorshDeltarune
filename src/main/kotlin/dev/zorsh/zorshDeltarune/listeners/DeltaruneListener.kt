package dev.zorsh.zorshDeltarune.listeners

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.vehicle.VehicleExitEvent

class DeltaruneListener : Listener {

    @EventHandler
    fun onVehicleExitEvent(e: VehicleExitEvent) {
        val player = e.exited
        if (player is Player) {
            player.sendMessage("Exit! || ${ZorshDeltarune.getDPlayer(player).locked}")
        }
    }

    @EventHandler
    fun onPlayerInputEvent(e: PlayerInputEvent) {
        val player = e.player
        player.sendMessage("W: ${e.input.isForward} | A: ${e.input.isLeft} | S: ${e.input.isBackward} | D: ${e.input.isRight}")
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