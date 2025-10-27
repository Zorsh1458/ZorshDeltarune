package dev.zorsh.zorshDeltarune.battle

abstract class DeltaruneEnemy(
    val hitpoints: Int,
) {

    var isAlive = true

    abstract suspend fun attack(onAttackEnds: () -> Unit = {})

    open fun die() {
        isAlive = false
    }
}