package dev.zorsh.zorshDeltarune.battle

import net.kyori.adventure.text.Component

abstract class DeltaruneEnemy(
    val name: Component,
    val hitpoints: Int,
    val encounterMessages: List<Component>
) {

    lateinit var myBattle: DeltaruneBattle

    var isAlive = true

    open fun askBoxSize(): Pair<Float, Float> {
        return 30f to 30f
    }

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}