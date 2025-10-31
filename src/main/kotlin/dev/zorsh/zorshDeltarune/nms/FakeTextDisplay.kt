package dev.zorsh.zorshDeltarune.nms

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeTextDisplay(
    entityId: Int,
    location: Location,
    transformation: Transformation,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null,
) : FakeDisplay(
    entityId,
    location,
    transformation,
    players,
    holder
) {}