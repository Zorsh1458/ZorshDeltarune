package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.animations.BattleBox
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
import org.joml.Quaternionf
import org.joml.Vector2d
import org.joml.Vector3f

object BattleLocation {
    val TEST = Location(Bukkit.getWorld("world"), 8.0, 100.0, 8.1)
    val UNDER_STATION = Location(Bukkit.getWorld("moon"), 952.0, 98.9, 1101.0)
}

abstract class DeltaruneBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) {

    val scope = CoroutineScope(Dispatchers.IO)

    val battleCenterLocation = BattleLocation.UNDER_STATION
    val battleBoxCenterLocation = battleCenterLocation + Vector3f(0f, 0f, 5f) + Vector3f(0.0f, 0.0f, 0.002f)

    private var onEndedAction = {}

    private val spawnedEntities = mutableSetOf<FakeDisplay>()

    val battleBox = BattleBox()

    var playersTurn = false

    fun damageHitbox(amount: Int, center: Vector2d, radius: Double): Boolean {
        var damagedAnyone = false
        for (dPlayer in players) {
            if (dPlayer.noDamageTicks <= 0 && dPlayer.soul?.location?.let { Vector2d(it.x - center.x, it.y - center.y).length() <= radius } == true) {
                dPlayer.damage(amount)
                damagedAnyone = true
            } else if (dPlayer.noDamageTicks <= 0 && dPlayer.soul?.location?.let { Vector2d(it.x - center.x, it.y - center.y).length() <= radius + 0.4 } == true) {
                dPlayer.tpGain()
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
        afterSpawn: (FakeTextDisplay) -> Unit = {}
    ) {
        PacketManager.spawnTextDisplay(
            loc,
            text,
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

    open fun destroyBattle() {
        val toDestroy = spawnedEntities.toList()
        for (ent in toDestroy) {
            try {
                ent.destroy()
            } catch (ignored: Exception) {}
        }
        spawnedEntities.clear()
        scope.cancel()
    }

    protected abstract fun startBattle()
}