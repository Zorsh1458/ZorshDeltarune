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
import java.util.UUID

@Suppress("UNUSED")
class PlayerUICanvas {
    var targetPlayers = mutableListOf<Player>()

    var objects = mutableListOf<FakeTextDisplay>()
    var savedObjects = mutableMapOf<String, FakeTextDisplay>()
    var objectsPerPlayer = mutableMapOf<UUID, MutableList<FakeTextDisplay>>()
    var savedObjectsPerPlayer = mutableMapOf<UUID, MutableMap<String, FakeTextDisplay>>()

    val TRANSLATION_BIAS = -0.18f

    fun initialize(players: List<Player>) {
        targetPlayers = players as MutableList<Player>
    }

    fun clear() {
        // Global objects
        objects.forEach {
            it.destroy()
        }
        objects.clear()
        savedObjects.clear()

        // Individual objects
        objectsPerPlayer.forEach { _, list ->
            list.forEach { it.destroy() }
        }
        objectsPerPlayer.clear()
        savedObjectsPerPlayer.clear()
    }

    fun clearPlayer(player: Player) {
        objectsPerPlayer[player.uniqueId]?.forEach {
            it.destroy()
        }
        objectsPerPlayer[player.uniqueId]?.clear()
        savedObjectsPerPlayer[player.uniqueId]?.clear()
    }

    fun updateCanvas(player: Player) {
        val playerEntities = objectsPerPlayer[player.uniqueId]?.map { it.entityId } ?: emptyList()
        val allEntities = objects.map { it.entityId }.toMutableList()
        allEntities.addAll(playerEntities)
        PacketManager.mountEntities(player.entityId, allEntities, listOf(player))
    }

    fun updateCanvas() {
        if (targetPlayers.isEmpty()) return
        targetPlayers.forEach { pl ->
            updateCanvas(pl)
        }
    }

    fun remove(objName: String, player: Player? = null) {
        if (player == null) {
            val obj = savedObjects[objName] ?: return
            objects.remove(obj)
            savedObjects.remove(objName)
            obj.destroy()
        } else {
            val obj = savedObjectsPerPlayer[player.uniqueId]?.get(objName) ?: return
            objectsPerPlayer[player.uniqueId]?.remove(obj)
            savedObjectsPerPlayer[player.uniqueId]?.remove(objName)
            obj.destroy()
        }
    }

    fun setZ(z: Int, objName: String, player: Player? = null) {
        fun getObj() =
            if (player == null) savedObjects[objName] else savedObjectsPerPlayer[player.uniqueId]?.get(objName)

        val obj = getObj() ?: return
        obj.changeOnlyTransformation(
            Transformation(
                Vector3f(obj.transformation.translation.x, obj.transformation.translation.y, z / 255f),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    fun setScale(sx: Float, sy: Float, objName: String, player: Player? = null) {
        fun getObj() =
            if (player == null) savedObjects[objName] else savedObjectsPerPlayer[player.uniqueId]?.get(objName)

        val obj = getObj() ?: return
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation,
                obj.transformation.leftRotation,
                Vector3f(sx * 2.5f, sy * 2.5f, obj.transformation.scale.z),
                obj.transformation.rightRotation
            )
        )
    }

    fun setPosition(px: Float, py: Float, objName: String, player: Player? = null) {
        fun getObj() =
            if (player == null) savedObjects[objName] else savedObjectsPerPlayer[player.uniqueId]?.get(objName)

        val obj = getObj() ?: return
        obj.changeOnlyTransformation(
            Transformation(
                Vector3f(px / 16f, py / 16f + TRANSLATION_BIAS, obj.transformation.translation.z),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    fun move(ox: Float, oy: Float, objName: String, player: Player? = null) {
        fun getObj() =
            if (player == null) savedObjects[objName] else savedObjectsPerPlayer[player.uniqueId]?.get(objName)

        val obj = getObj() ?: return
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation + Vector3f(ox / 16f, oy / 16f, 0f),
                obj.transformation.leftRotation,
                obj.transformation.scale,
                obj.transformation.rightRotation
            )
        )
    }

    private fun handleDraw(
        drawFunction: (List<Player>, (FakeTextDisplay) -> Unit) -> Unit,
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit)
    {
        if (player == null) {
            if (targetPlayers.isEmpty()) return
            drawFunction(targetPlayers) { ent ->
                objects += ent
                if (saveAs != null) {
                    savedObjects[saveAs] = ent
                }
                updateCanvas()
                afterSpawn()
            }
        } else {
            drawFunction(listOf(player)) { ent ->
                objectsPerPlayer.getOrPut(player.uniqueId, { return@getOrPut mutableListOf() }) += ent
                if (saveAs != null) {
                    savedObjectsPerPlayer.getOrPut(player.uniqueId, { return@getOrPut mutableMapOf() })[saveAs] = ent
                }
                updateCanvas(player)
                afterSpawn()
            }
        }
    }

    fun drawRect(
        sx: Float,
        sy: Float,
        dx: Float,
        dy: Float,
        z: Int,
        hexColor: String,
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit = {},
    ) {
        fun drawToPlayers(players: List<Player>, after: (FakeTextDisplay) -> Unit) {
            if (players.isEmpty()) return
            val actualPlayer = players[0]
            val loc = actualPlayer.eyeLocation.clone()
            loc.yaw = 0f
            loc.pitch = 0f
            val scaleX = dx - sx + 1
            val scaleY = dy - sy + 1
            PacketManager.spawnTextDisplay(
                loc,
                Component.text("\uF001").font("space:dbattle").color(hexColor),
                players,
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
                after(ent)
            }
        }

        handleDraw(::drawToPlayers, saveAs, player, afterSpawn)
    }

    fun drawMouse(
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit = {},
    ) {
        fun drawToPlayers(players: List<Player>, after: (FakeTextDisplay) -> Unit) {
            if (players.isEmpty()) return
            val actualPlayer = players[0]
            val loc = actualPlayer.eyeLocation.clone()
            loc.yaw = 0f
            loc.pitch = 0f
            PacketManager.spawnTextDisplay(
                loc,
                CanvasSprite.MOUSE.toTextValue().color("#ffffff"),
                players,
                FakeDisplayData(
                    Transformation(
                        Vector3f(0f, TRANSLATION_BIAS, 1 / 255f),
                        AxisAngle4f(),
                        Vector3f(5f, 5f, 1f),
                        AxisAngle4f()
                    ),
                    opacity = 252.toByte()
                ),
                false,
                TextDisplay.TextAlignment.CENTER,
                1000,
                false
            ) { ent ->
                after(ent)
            }
        }

        handleDraw(::drawToPlayers, saveAs, player, afterSpawn)
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
        player: Player? = null,
        afterSpawn: () -> Unit = {},
    ) {
        fun drawToPlayers(players: List<Player>, after: (FakeTextDisplay) -> Unit) {
            if (players.isEmpty()) return
            val actualPlayer = players[0]
            val loc = actualPlayer.eyeLocation.clone()
            loc.yaw = 0f
            loc.pitch = 0f
            PacketManager.spawnTextDisplay(
                loc,
                sprite.toTextValue().color(hexColor),
                players,
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
                after(ent)
            }
        }

        handleDraw(::drawToPlayers, saveAs, player, afterSpawn)
    }
}