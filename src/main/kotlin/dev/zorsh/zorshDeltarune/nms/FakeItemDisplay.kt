package dev.zorsh.zorshDeltarune.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeItemDisplay(
    entityId: Int,
    location: Location,
    transformation: Transformation,
    teleportDuration: Int,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null,
) : FakeDisplay(
    entityId,
    location,
    transformation,
    teleportDuration,
    players,
    holder
) {}