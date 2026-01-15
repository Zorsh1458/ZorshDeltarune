package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.*
import net.kyori.adventure.text.Component
import org.joml.Vector3f

abstract class DeltaruneEnemy(
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