package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeDisplay
import dev.zorsh.zorshDeltarune.nms.FakeItemDisplay
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object BattleLocation {
    val TEST = Location(Bukkit.getWorld("world"), 8.0, 100.0, 8.1)
    val UNDER_STATION = Location(Bukkit.getWorld("moon"), 952.0, 99.2, 1101.0)
}

abstract class DeltaruneBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) {

    val scope = CoroutineScope(Dispatchers.IO)

    val sceneScale = Vector3f(-0.04f, -0.04f, 0.04f)

    val sceneOffset = Vector3d(0.0 * sceneScale.x, -0.5 * sceneScale.y, -0.45)

    val battleCenterLocation = BattleLocation.UNDER_STATION
    val battleBoxCenterLocation = battleCenterLocation + Vector3f(0f, 0f, 5f) + Vector3f(0.0f, 0.0f, 0.002f)

    private var onEndedAction = {}

    protected val shulkerHitboxes = mutableSetOf<Int>()

    private val spawnedEntities = mutableSetOf<FakeDisplay>()

    var playersTurn = false

    fun damageHitbox(amount: Int, center: Vector3f, radius: Double): Boolean {
        var damagedAnyone = false
        for (dPlayer in players) {
            if (dPlayer.soul != null && dPlayer.player != null && dPlayer.canMoveSoul) {
                val vec = Vector3f(
                    dPlayer.soul!!.location.x.toFloat(),
                    dPlayer.soul!!.location.y.toFloat(),
                    dPlayer.soul!!.location.z.toFloat()
                )
                val correctedCent = Vector3f(
                    center.x + dPlayer.player!!.eyeLocation.x.toFloat(),
                    dPlayer.soul!!.location.y.toFloat(),
                    center.z + dPlayer.player!!.eyeLocation.z.toFloat()
                )
//                Bukkit.getConsoleSender().sendMessage("${vec.distance(correctedCent)}")
                if (dPlayer.noDamageTicks <= 0 && vec.distance(correctedCent) <= radius) {
                    dPlayer.damage(amount)
                    dPlayer.shakingTime = 7
                    dPlayer.shakingMult = 3.0
                    damagedAnyone = true
                } else if (dPlayer.noDamageTicks <= 0 && vec.distance(correctedCent) <= radius + 0.083) {
                    dPlayer.tpGain()
                }
            }
        }
        return damagedAnyone
    }

    fun start(onEnded: () -> Unit) {
        onEndedAction = onEnded
        for (enemy in enemies) {
            enemy.myBattle = this
        }
        startBattle()
    }

    open fun end() {
        for (enemy in enemies) {
            if (enemy.isAlive) {
                try {
                    enemy.die()
                } catch (ignored: Exception) {}
            }
        }
        for (pl in players) {
            try {
                pl.freeFromBattle()
            } catch (ignored: Exception) {}
        }
        destroyBattle()
        onEndedAction()
//        scope.cancel()
    }

    fun newProjectile(
        projectile: Int,
        position: Vector3f = Vector3f(0f),
        afterSpawn: (FakeTextDisplay) -> Unit
    ) {
        val offset = sceneOffset
        val scale = sceneScale
        val loc = battleCenterLocation
        loc.pitch = -90f
        newTextDisplay(
            loc,
            Component.text("${(61440 + projectile).toChar()}").font("space:projectiles"),
            data = FakeDisplayData(Transformation(
                position * scale + Vector3f(0f, -0.25f, offset.z.toFloat() - 2.3f),
                AxisAngle4f(),
                Vector3f(0f),
                AxisAngle4f()
            ), teleportDuration = 1
            ),
            mountTo = true,
            seeThrough = true
        ) { entity ->
            afterSpawn(entity)
//            runLater(10) {
//                entity.changeTransformation(
//                    Transformation(
//                        Vector3f(
//                            entity.transformation.translation.x,
//                            entity.transformation.translation.y,
//                            -1.7f + battleCenterLocation.y.toFloat() + sceneOffset.z.toFloat() - entity.location.y.toFloat()
//                        ),
//                        entity.transformation.leftRotation,
//                        entity.transformation.scale,
//                        entity.transformation.rightRotation
//                    )
//                )
//            }
        }
    }

    fun newHitboxEntity(
        loc: Location,
        playerToShow: List<Player> = players.mapNotNull { it.player }
    ) {
        PacketManager.spawnHitbox(loc, playerToShow) { anchor, shulker ->
            shulkerHitboxes += anchor
            shulkerHitboxes += shulker
        }
    }

    fun newShaderEffector(
        loc: Location,
        playerToShow: List<Player> = players.mapNotNull { it.player }
    ) {
        PacketManager.spawnShaderEffector(
            loc,
            playerToShow,
        ) { entity ->
            spawnedEntities += entity
            entity.holder = spawnedEntities
        }
    }

    fun newItemDisplay(
        loc: Location,
        item: ItemStack,
        playerToShow: List<Player> = players.mapNotNull { it.player },
        data: FakeDisplayData = FakeDisplayData(
            Transformation(
                Vector3f(0f),
                Quaternionf(0f, 0f, 0f, 1f),
                Vector3f(1f),
                Quaternionf(0f, 0f, 0f, 1f)
            )),
        mountTo: Boolean,
        afterSpawn: (FakeItemDisplay) -> Unit = {}
    ) {
        PacketManager.spawnItemDisplay(
            loc,
            item,
            playerToShow,
            data
        ) { entity ->
            spawnedEntities += entity
            entity.holder = spawnedEntities
            if (mountTo) {
                players.forEach { it.mountEntity(entity)}
            }
            afterSpawn(entity)
        }
    }

    fun newTextDisplay(
        loc: Location,
        text: Component,
        playerToShow: List<Player> = players.mapNotNull { it.player },
        data: FakeDisplayData = FakeDisplayData(
            Transformation(
            Vector3f(0f),
            Quaternionf(0f, 0f, 0f, 1f),
            Vector3f(1f),
            Quaternionf(0f, 0f, 0f, 1f)
        )),
        mountTo: Boolean,
        seeThrough: Boolean = false,
        afterSpawn: (FakeTextDisplay) -> Unit = {}
    ) {
        PacketManager.spawnTextDisplay(
            loc,
            text,
            playerToShow,
            data,
            seeThrough
        ) { entity ->
            spawnedEntities += entity
            entity.holder = spawnedEntities
            if (mountTo) {
                players.forEach { it.mountEntity(entity)}
            }
            afterSpawn(entity)
        }
    }

    open fun destroyBattle() {
        val toDestroy = spawnedEntities.toList()
        for (ent in toDestroy) {
            try {
                ent.destroy()
            } catch (ignored: Exception) {}
        }
        spawnedEntities.clear()
        for (ent in shulkerHitboxes) {
            try {
                PacketManager.removeEntity(ent, players.mapNotNull { it.player })
            } catch (ignored: Exception) {}
        }
        shulkerHitboxes.clear()
        scope.cancel()
    }

    protected abstract fun startBattle()
}