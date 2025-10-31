package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.utils.*
import org.joml.Vector3d

abstract class DeltaruneEnemy(
    val hitpoints: Int,
) {

    lateinit var myBattle: DeltaruneBattle

    val projectileCenterLocation by lazy { myBattle.battleBoxCenterLocation + Vector3d(0.0, 0.0, 0.00096) }

    var isAlive = true

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}