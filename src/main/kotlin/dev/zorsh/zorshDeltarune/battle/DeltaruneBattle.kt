package dev.zorsh.zorshDeltarune.battle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.bukkit.Bukkit
import org.bukkit.Location

abstract class DeltaruneBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) {

    val scope = CoroutineScope(Dispatchers.IO)

    val battleCenterLocation = Location(Bukkit.getWorld("world"), 0.0, 100.0, 0.0)

    private var onEndedAction = {}

    fun start(onEnded: () -> Unit) {
        onEndedAction = onEnded
        for (enemy in enemies) {
            enemy.myBattle = this
        }
        startBattle()
    }

    fun end() {
        for (enemy in enemies) {
            if (enemy.isAlive) {
                enemy.die()
            }
        }
        for (pl in players) {
            pl.freeFromBattle()
        }
        scope.cancel()
        destroyBattle()
        onEndedAction()
    }

    abstract fun destroyBattle()

    protected abstract fun startBattle()
}