package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.utils.*
import org.joml.Vector3f

abstract class DeltaruneEnemy(
    val hitpoints: Int,
) {

    lateinit var myBattle: DeltaruneBattle

    val projectileCenterLocation by lazy { myBattle.battleBoxCenterLocation - Vector3f(0.0f, 0.0f, 0.0001f) }

    var isAlive = true

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}