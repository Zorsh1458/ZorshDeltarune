package dev.zorsh.zorshDeltarune.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import net.minecraft.world.phys.Vec3

class FakeTextDisplay(
    private val entityId: Int,
    var location: Location,
    private val players: List<Player>,
    var holder: MutableSet<FakeTextDisplay>? = null,
) {

    private var exists = true

    fun destroy() {
        if (exists) {
            PacketManager.removeEntity(entityId, players)
            exists = false
            holder?.remove(this)
        }
    }

    fun teleport(newLocation: Location) {
        PacketManager.teleportEntity(entityId, newLocation, Vec3(0.0, 0.0, 0.0), players)
        location = newLocation
    }
}