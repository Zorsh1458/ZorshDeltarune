package dev.zorsh.zorshDeltarune.nms

import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeItemDisplay(
    entityId: Int,
    transformation: Transformation,
    teleportDuration: Int,
    interpolationDuration: Int,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null
) : FakeDisplay(
    entityId,
    transformation,
    teleportDuration,
    interpolationDuration,
    players,
    holder
)