package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.PacketManager
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.joml.Vector3d
import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class DeltarunePlayer(val player: Player) {

    var locked = false

    fun freeFromBattle() {
        locked = false
        player.stopAllSounds()
    }

    fun lockInBattle(location: Location) {
        val anchor = location.world?.spawnEntity(location, EntityType.BLOCK_DISPLAY) ?: return
        anchor.isPersistent = false

        locked = true
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    locked = false
                }

                if (!locked) {
                    if (anchor.isValid) {
                        anchor.remove()
                    }
                    cancel()
                }

                if (player.vehicle == null || player.vehicle != anchor) {
                    anchor.addPassenger(player)
                    player.sendActionBar(Component.text(""))
                }

                PacketManager.playerLookAt(player.location + Vector3d(0.0, 0.0, 3.0), listOf(player))
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }
}