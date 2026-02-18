package dev.zorsh.zorshDeltarune.nms

import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runRepeating
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.util.ARGBLike
import net.kyori.adventure.util.RGBLike
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.Vector3f

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

    fun animateBattleText(text: Component) {
        val content = PlainTextComponentSerializer.plainText().serialize(text)
        val style = text.style()
        val updating = Component.text()
//        updating.append(Component.text(" ".repeat(64) + '\n').color(TextColor.color(0, 0, 0)))
        updating.append(Component.text(" ".repeat(64) + '\n'))
        runRepeating(content.length) { ind ->
            if (content[ind] == '\n') {
                transformation = Transformation(
                    transformation.translation + Vector3f(0f, 0.02f, 0f),
                    transformation.leftRotation,
                    transformation.scale,
                    transformation.rightRotation
                )
            }
            updating.append(Component.text(content[ind]))
            PacketManager.setTextDisplayMetadata(
                entityId,
                updating.style(style).shadowColor(ShadowColor.shadowColor(0, 0, 64, 255)).build(),
                transformation,
                players,
                interpolationDuration,
                teleportDuration,
                opacity
            )
        }
    }
}