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

    data class SpriteScalingHolder(
        val entity: FakeTextDisplay,
        val scaling: Pair<Float, Float>
    )

    var objects = mutableListOf<FakeTextDisplay>()

    // savedObjects[name] = (entity: display, scaling: (x, y))
    var savedObjects = mutableMapOf<String, SpriteScalingHolder>()
    var objectsPerPlayer = mutableMapOf<UUID, MutableList<FakeTextDisplay>>()

    // savedObjects[player.uniqueId][name] = (entity: display, scaling: (x, y))
    var savedObjectsPerPlayer = mutableMapOf<UUID, MutableMap<String, SpriteScalingHolder>>()

    val TRANSLATION_BIAS = -0.2f

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

    fun remove(objName: String, playerUUID: UUID? = null) {
        if (playerUUID == null) {
            val pair = savedObjects[objName] ?: return
            val obj = pair.entity
            objects.remove(obj)
            savedObjects.remove(objName)
            obj.destroy()
        } else {
            val pair = savedObjectsPerPlayer[playerUUID]?.get(objName) ?: return
            val obj = pair.entity
            objectsPerPlayer[playerUUID]?.remove(obj)
            savedObjectsPerPlayer[playerUUID]?.remove(objName)
            obj.destroy()
        }
    }

    fun setZ(z: Int, objName: String, playerUUID: UUID? = null) {
        fun getObj() =
            if (playerUUID == null) savedObjects[objName]?.entity else savedObjectsPerPlayer[playerUUID]?.get(objName)?.entity

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

    fun setRotation(angle: Float, objName: String, playerUUID: UUID? = null) {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: return

        val obj = holder.entity
        val scaling = holder.scaling
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation,
                AxisAngle4f(angle, 0f, 0f, 1f),
                obj.transformation.scale,
                AxisAngle4f(obj.transformation.rightRotation)
            )
        )
    }

    fun rotate(angle: Float, objName: String, playerUUID: UUID? = null) {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: return

        val obj = holder.entity
        val scaling = holder.scaling
        val currentAngle = AxisAngle4f(obj.transformation.leftRotation).angle
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation,
                AxisAngle4f(currentAngle + angle, 0f, 0f, 1f),
                obj.transformation.scale,
                AxisAngle4f(obj.transformation.rightRotation)
            )
        )
    }

    fun setSprite(sprite: CanvasSprite, color: ShaderTextColor, objName: String, playerUUID: UUID? = null) {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: return

        val obj = holder.entity
        val scaling = holder.scaling
        val size = getScale(objName, playerUUID)
        obj.changeText(sprite.toTextValue().color(color.value))
        if (playerUUID == null) {
            savedObjects[objName] = SpriteScalingHolder(obj, sprite.getSizeRatios())
        } else {
            savedObjectsPerPlayer[playerUUID]?.set(objName, SpriteScalingHolder(obj, sprite.getSizeRatios()))
        }
        setScale(size.first, size.second, objName, playerUUID)
    }

    fun setText(text: Component, objName: String, playerUUID: UUID? = null) {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: return

        val obj = holder.entity
        val scaling = holder.scaling
        val size = getScale(objName, playerUUID)
        obj.changeText(text)
        if (playerUUID == null) {
            savedObjects[objName] = SpriteScalingHolder(obj, 1f to 1f)
        } else {
            savedObjectsPerPlayer[playerUUID]?.set(objName, SpriteScalingHolder(obj, 1f to 1f))
        }
        setScale(size.first, size.second, objName, playerUUID)
    }

    fun hasObject(objName: String, playerUUID: UUID? = null): Boolean {
        if (playerUUID == null) {
            return savedObjects.contains(objName)
        } else {
            return savedObjectsPerPlayer[playerUUID]?.contains(objName) == true
        }
    }

    fun setScale(sx: Float, sy: Float, objName: String, playerUUID: UUID? = null) {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: return

        val obj = holder.entity
        val scaling = holder.scaling
        obj.changeOnlyTransformation(
            Transformation(
                obj.transformation.translation,
                obj.transformation.leftRotation,
                Vector3f(sx * scaling.first * 2.5f, sy * scaling.second * 2.5f, obj.transformation.scale.z),
                obj.transformation.rightRotation
            )
        )
    }

    fun getScale(objName: String, playerUUID: UUID? = null): Pair<Float, Float> {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: throw IllegalStateException("Object $objName not found")

        val obj = holder.entity
        val scaling = holder.scaling

        return obj.transformation.scale.x / scaling.first / 2.5f to obj.transformation.scale.y / scaling.second / 2.5f
    }

    fun setPosition(px: Float, py: Float, objName: String, playerUUID: UUID? = null) {
        fun getObj() =
            if (playerUUID == null) savedObjects[objName]?.entity else savedObjectsPerPlayer[playerUUID]?.get(objName)?.entity

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

    fun getPosition(objName: String, playerUUID: UUID? = null): Pair<Float, Float> {
        val holder =
            (if (playerUUID == null) savedObjects[objName] else savedObjectsPerPlayer[playerUUID]?.get(objName)) ?: throw IllegalStateException("Object $objName not found")

        val obj = holder.entity

        return obj.transformation.translation.x * 16f to (obj.transformation.translation.y - TRANSLATION_BIAS) * 16f
    }

    fun move(ox: Float, oy: Float, objName: String, playerUUID: UUID? = null) {
        fun getObj() =
            if (playerUUID == null) savedObjects[objName]?.entity else savedObjectsPerPlayer[playerUUID]?.get(objName)?.entity

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
        scaling: Pair<Float, Float> = 1f to 1f,
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit)
    {
        if (player == null) {
            if (targetPlayers.isEmpty()) return
            drawFunction(targetPlayers) { ent ->
                objects += ent
                if (saveAs != null) {
                    savedObjects[saveAs] = SpriteScalingHolder(ent, scaling)
                }
                updateCanvas()
                afterSpawn()
            }
        } else {
            drawFunction(listOf(player)) { ent ->
                objectsPerPlayer.getOrPut(player.uniqueId, { return@getOrPut mutableListOf() }) += ent
                if (saveAs != null) {
                    savedObjectsPerPlayer.getOrPut(player.uniqueId, { return@getOrPut mutableMapOf() })[saveAs] = SpriteScalingHolder(ent, scaling)
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
        color: ShaderTextColor,
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
                Component.text("\uF001").font("space:dbattle").color(color.value),
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

        handleDraw(::drawToPlayers, 1f to 1f, saveAs, player, afterSpawn)
    }

    fun drawMouse(
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit = {},
    ) {
        val scaling = CanvasSprite.MOUSE.getSizeRatios()
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
                        Vector3f(5f * scaling.first, 5f * scaling.second, 1f),
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

        handleDraw(::drawToPlayers, scaling, saveAs, player, afterSpawn)
    }

    fun drawSprite(
        px: Float,
        py: Float,
        sx: Float,
        sy: Float,
        z: Int,
        sprite: CanvasSprite,
        color: ShaderTextColor,
        saveAs: String? = null,
        player: Player? = null,
        afterSpawn: () -> Unit = {},
    ) {
        val scaling = sprite.getSizeRatios()
        fun drawToPlayers(players: List<Player>, after: (FakeTextDisplay) -> Unit) {
            if (players.isEmpty()) return
            val actualPlayer = players[0]
            val loc = actualPlayer.eyeLocation.clone()
            loc.yaw = 0f
            loc.pitch = 0f
            PacketManager.spawnTextDisplay(
                loc,
                sprite.toTextValue().color(color.value),
                players,
                FakeDisplayData(
                    Transformation(
                        Vector3f(px / 16f, py / 16f + TRANSLATION_BIAS, z / 255f),
                        AxisAngle4f(),
                        Vector3f(2.5f * scaling.first * sx, 2.5f * scaling.second * sy, 1f),
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

        handleDraw(::drawToPlayers, scaling, saveAs, player, afterSpawn)
    }

    fun drawText(
        px: Float,
        py: Float,
        sx: Float,
        sy: Float,
        z: Int,
        text: Component,
        color: ShaderTextColor?,
        alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
        lineWidth: Int = 1000,
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
                if (color != null) text.color(color.value) else text,
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
                alignment,
                lineWidth,
                false
            ) { ent ->
                after(ent)
            }
        }

        handleDraw(::drawToPlayers, 1f to 1f, saveAs, player, afterSpawn)
    }
}