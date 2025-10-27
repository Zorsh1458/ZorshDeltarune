package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class DeltarunePlayer(val player: Player) {

    var locked = false

    fun freeFromBattle() {
        locked = false
    }

    fun lockInBattle(location: Location) {
        val anchor = location.world.spawnEntity(location, EntityType.BLOCK_DISPLAY)
        anchor.isPersistent = false

        locked = true
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    locked = false
                }

                if (!locked) {
                    cancel()
                }

                if (player.vehicle == null || player.vehicle != anchor) {
                    anchor.addPassenger(player)
                }
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }
}