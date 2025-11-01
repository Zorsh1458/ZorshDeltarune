package dev.zorsh.zorshDeltarune.battle

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
import org.joml.Vector3f

abstract class DeltaruneBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) {

    val scope = CoroutineScope(Dispatchers.IO)

    val battleCenterLocation = Location(Bukkit.getWorld("world"), 8.0, 100.0, 8.1)
    val battleBoxCenterLocation = battleCenterLocation + Vector3f(0f, 0f, 5f) + Vector3f(0.0f, 0.0f, 0.002f)

    private var onEndedAction = {}

    private val spawnedEntities = mutableSetOf<FakeDisplay>()

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
                enemy.die()
            }
        }
        for (pl in players) {
            pl.freeFromBattle()
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
    }

    protected abstract fun startBattle()
}