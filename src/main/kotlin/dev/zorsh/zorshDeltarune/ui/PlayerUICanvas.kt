package dev.zorsh.zorshDeltarune.ui

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.color
import dev.zorsh.zorshDeltarune.utils.font
import dev.zorsh.zorshDeltarune.utils.plus
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

@Suppress("UNUSED")
class PlayerUICanvas {
    var targetPlayers = mutableListOf<Player>()

    var objects = mutableListOf<FakeTextDisplay>()
    var savedObjects = mutableMapOf<String, FakeTextDisplay>()

    val TRANSLATION_BIAS = -0.18f

    fun initialize(players: List<Player>) {
        targetPlayers = players as MutableList<Player>
    }

    fun clear() {
        objects.forEach {
            it.destroy()
        }
        objects.clear()
        savedObjects.clear()
    }

    fun updateCanvas() {
        if (targetPlayers.isEmpty()) return
        targetPlayers.forEach { pl ->
            PacketManager.mountEntities(pl.entityId, objects.map { it.entityId }, listOf(pl))
        }
    }

    fun remove(objName: String) {
        val obj = savedObjects[objName] ?: return
        objects.remove(obj)
        savedObjects.remove(objName)
        obj.destroy()
    }

    fun setZ(z: Int, objName: String) {
        val obj = savedObjects[objName] ?: return
        obj.changeOnlyTransformation(
            Transformation(
                Vector3f(obj.transformation.translation.x, obj.transformation.translation.y, z / 255f),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    fun setScale(sx: Float, sy: Float, objName: String) {
        val obj = savedObjects[objName] ?: return
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation,
                obj.transformation.leftRotation,
                Vector3f(sx * 2.5f, sy * 2.5f, obj.transformation.scale.z),
                obj.transformation.rightRotation
            )
        )
    }

    fun setPosition(px: Float, py: Float, objName: String) {
        val obj = savedObjects[objName] ?: return
        obj.changeOnlyTransformation(
            Transformation(
                Vector3f(px / 16f, py / 16f + TRANSLATION_BIAS, obj.transformation.translation.z),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    fun move(ox: Float, oy: Float, objName: String) {
        val obj = savedObjects[objName] ?: return
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation + Vector3f(ox / 16f, oy / 16f, 0f),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    fun drawRect(
        sx: Float,
        sy: Float,
        dx: Float,
        dy: Float,
        z: Int,
        hexColor: String,
        saveAs: String? = null,
        afterSpawn: () -> Unit = {}
    ) {
        if (targetPlayers.isEmpty()) return
        val actualPlayer = targetPlayers[0]
        val loc = actualPlayer.eyeLocation.clone()
        loc.yaw = 0f
        loc.pitch = 0f
        val scaleX = dx - sx + 1
        val scaleY = dy - sy + 1
        PacketManager.spawnTextDisplay(
            loc,
            Component.text("\uF001").font("space:dbattle").color(hexColor),
            targetPlayers,
            FakeDisplayData(
                Transformation(
                    Vector3f(sx / 16f, sy / 16f + TRANSLATION_BIAS, z / 255f),
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
            if (saveAs != null) {
                savedObjects[saveAs] = ent
            }
            updateCanvas()
            afterSpawn()
        }
    }

    fun drawMouse(
        saveAs: String? = null,
        afterSpawn: () -> Unit = {}
    ) {
        if (targetPlayers.isEmpty()) return
        val actualPlayer = targetPlayers[0]
        val loc = actualPlayer.eyeLocation.clone()
        loc.yaw = 0f
        loc.pitch = 0f
        PacketManager.spawnTextDisplay(
            loc,
            CanvasSprite.MOUSE.toTextValue().color("#ffffff"),
            targetPlayers,
            FakeDisplayData(
                Transformation(
                    Vector3f(0f, TRANSLATION_BIAS, 1 / 255f),
                    AxisAngle4f(),
                    Vector3f(2.5f, 2.5f, 1f),
                    AxisAngle4f()
                ),
                opacity = 252.toByte()
            ),
            false,
            TextDisplay.TextAlignment.CENTER,
            1000,
            false
        ) { ent ->
            objects += ent
            if (saveAs != null) {
                savedObjects[saveAs] = ent
            }
            updateCanvas()
            afterSpawn()
        }
    }

    fun drawSprite(
        px: Float,
        py: Float,
        sx: Float,
        sy: Float,
        z: Int,
        sprite: CanvasSprite,
        hexColor: String,
        saveAs: String? = null,
        afterSpawn: () -> Unit = {}
    ) {
        if (targetPlayers.isEmpty()) return
        val actualPlayer = targetPlayers[0]
        val loc = actualPlayer.eyeLocation.clone()
        loc.yaw = 0f
        loc.pitch = 0f
        PacketManager.spawnTextDisplay(
            loc,
            sprite.toTextValue().color(hexColor),
            targetPlayers,
            FakeDisplayData(
                Transformation(
                    Vector3f(px / 16f, py / 16f + TRANSLATION_BIAS, z / 255f),
                    AxisAngle4f(),
                    Vector3f(2.5f * sx, 2.5f * sy, 1f),
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
            if (saveAs != null) {
                savedObjects[saveAs] = ent
            }
            updateCanvas()
            afterSpawn()
        }
    }
}