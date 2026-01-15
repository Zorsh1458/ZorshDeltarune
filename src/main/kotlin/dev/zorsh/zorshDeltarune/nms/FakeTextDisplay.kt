package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation

class FakeTextDisplay(
    entityId: Int,
    var text: Component,
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
    override fun changeTransformation(newTransformation: Transformation, newText: Component, newOpacity: Byte) {
        val new = newText.replaceText { builder ->
            builder.match("___DEFAULT_TEXT___")
                .replacement(text)
        }
        PacketManager.setTextDisplayMetadata(entityId, new, newTransformation, players, interpolationDuration, teleportDuration, newOpacity)
        transformation = newTransformation
        opacity = newOpacity
        text = new
    }
}