package dev.zorsh.zorshDeltarune.nms

import org.bukkit.Location
import org.bukkit.entity.Player

class FakeItemDisplay(
    entityId: Int,
    location: Location,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null,
) : FakeDisplay(
    entityId,
    location,
    players,
    holder
) {}