package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.utils.runLater
import net.kyori.adventure.text.Component
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

abstract class FakeDisplay(
    protected val entityId: Int,
    var location: Location,
    var transformation: Transformation,
    protected val teleportDuration: Int,
    protected val interpolationDuration: Int,
    protected val players: List<Player>,
    var holder: MutableSet<FakeDisplay>? = null,
) {

    private var exists = true

    open fun destroy() {
        if (exists) {
            exists = false
            holder?.remove(this)
            val trans = Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(0f),
                AxisAngle4f(),
            )
            changeTransformation(trans)
            repeat(20) { i ->
                runLater(i * 20L) {
                    changeTransformation(trans)
                    PacketManager.removeEntity(entityId, players)
                }
            }
            runLater(800L) {
                PacketManager.removeEntity(entityId, players)
            }
        }
    }

    open fun teleport(newLocation: Location) {
        PacketManager.teleportEntity(entityId, newLocation, Vec3(0.0, 0.0, 0.0), players)
        location = newLocation
    }

    open fun changeTransformation(newTransformation: Transformation, newOpacity: Byte = 255.toByte()) {
        PacketManager.setTransformation(entityId, newTransformation, players, interpolationDuration, teleportDuration)
        transformation = newTransformation
    }
}