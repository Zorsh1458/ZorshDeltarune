package dev.zorsh.zorshDeltarune.battle.enemy

import dev.zorsh.zorshDeltarune.battle.INeverlandBattle
import net.kyori.adventure.text.Component

abstract class DeltaruneEnemy(
    val name: Component,
    val hitpoints: Int,
    val encounterMessages: List<Component>
) {

    lateinit var myBattle: INeverlandBattle

    var isAlive = true

    open fun askBoxSize(): Pair<Float, Float> {
        return 60f to 60f
    }

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}