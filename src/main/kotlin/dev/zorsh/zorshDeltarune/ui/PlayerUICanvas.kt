package dev.zorsh.zorshDeltarune.ui

import dev.zorsh.zorshDeltarune.nms.FakeInteraction
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.color
import dev.zorsh.zorshDeltarune.utils.font
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

class PlayerUICanvas {
    var targetPlayer: Player? = null
    var canvasHolder: FakeInteraction? = null

    var objects = mutableListOf<FakeTextDisplay>()

    fun initialize(player: Player) {
        targetPlayer = player
        PacketManager.spawnInteraction(player.eyeLocation, listOf(player), 0f, -0.18f) { inter ->
            canvasHolder = inter
            PacketManager.mountEntities(player.entityId, listOf(canvasHolder!!.entityId), listOf(player))
            updateCanvas()
        }
    }

    fun clear() {
        objects.forEach {
            it.destroy()
        }
        objects.clear()
    }

    fun destroy() {
        clear()
        canvasHolder?.destroy()
        canvasHolder = null
    }

    fun updateCanvas() {
        if (targetPlayer == null) return
        if (canvasHolder == null) return
        val actualPlayer = targetPlayer!!
        PacketManager.mountEntities(canvasHolder!!.entityId, objects.map { it.entityId }, listOf(actualPlayer))
    }

    fun drawRect(sx: Float, sy: Float, dx: Float, dy: Float, z: Int, hexColor: String) {
        if (targetPlayer == null) return
        if (canvasHolder == null) return
        val actualPlayer = targetPlayer!!
        val loc = actualPlayer.eyeLocation.clone()
        loc.yaw = 0f
        loc.pitch = 0f
        val scaleX = dx - sx + 1
        val scaleY = dy - sy + 1
        PacketManager.spawnTextDisplay(
            loc,
            Component.text("\uF001").font("space:dbattle").color(hexColor),
            listOf(actualPlayer),
            FakeDisplayData(
                Transformation(
                    Vector3f(sx / 16f, sy / 16f, z / 255f),
                    AxisAngle4f(),
                    Vector3f(2.5f * scaleX, 2.5f * scaleY, 1f),
                    AxisAngle4f()
                ),
                opacity = 253.toByte()
            ),
            false,
            TextDisplay.TextAlignment.CENTER,
            1000,
            false
            ) { ent ->
            objects += ent
            updateCanvas()
        }
    }
}