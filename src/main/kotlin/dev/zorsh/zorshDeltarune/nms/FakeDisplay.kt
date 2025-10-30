package dev.zorsh.zorshDeltarune.nms

import net.minecraft.server.packs.repository.Pack
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

abstract class FakeDisplay(
    private val entityId: Int,
    var location: Location,
    private val players: List<Player>,
    var holder: MutableSet<FakeDisplay>? = null,
) {

    private var exists = true

    open fun destroy() {
        if (exists) {
            PacketManager.removeEntity(entityId, players)
            exists = false
            holder?.remove(this)
        }
    }

    open fun teleport(newLocation: Location) {
        PacketManager.teleportEntity(entityId, newLocation, Vec3(0.0, 0.0, 0.0), players)
        location = newLocation
    }

    open fun changeTransformation(newTransformation: Transformation) {
        PacketManager.setTransformation(entityId, newTransformation, players)
    }
}