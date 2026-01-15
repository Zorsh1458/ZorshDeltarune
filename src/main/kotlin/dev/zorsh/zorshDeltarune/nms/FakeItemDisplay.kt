package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeItemDisplay(
    entityId: Int,
    location: Location,
    transformation: Transformation,
    teleportDuration: Int,
    interpolationDuration: Int,
    players: List<Player>,
    holder: MutableSet<FakeDisplay>? = null
) : FakeDisplay(
    entityId,
    location,
    transformation,
    teleportDuration,
    interpolationDuration,
    players,
    holder
)