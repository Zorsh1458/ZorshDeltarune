package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import org.joml.Vector3f

abstract class DeltaruneEnemy(
    val hitpoints: Int,
    val encounterMessages: List<Component>
) {

    lateinit var myBattle: DeltaruneBattle

    val projectileCenterLocation by lazy { myBattle.battleBoxCenterLocation + Vector3f(0.0f, 1.5f, -0.005f) }

    var isAlive = true

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}