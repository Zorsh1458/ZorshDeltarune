package dev.zorsh.zorshDeltarune.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeTextDisplay(
    entityId: Int,
    location: Location,
    transformation: Transformation,
    teleportDuration: Int,
    interpolationDuration: Int,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null,
    var opacity: Byte
) : FakeDisplay(
    entityId,
    location,
    transformation,
    teleportDuration,
    interpolationDuration,
    players,
    holder
) {
    override fun changeTransformation(newTransformation: Transformation, newOpacity: Byte) {
        PacketManager.setTextDisplayMetadata(entityId, newTransformation, players, interpolationDuration, teleportDuration, newOpacity)
        transformation = newTransformation
        opacity = newOpacity
    }
}