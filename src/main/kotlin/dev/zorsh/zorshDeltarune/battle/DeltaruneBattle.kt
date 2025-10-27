package dev.zorsh.zorshDeltarune.battle

abstract class DeltaruneBattle(val players: List<DeltarunePlayer>, val enemies: List<DeltaruneEnemy>) {

    private var onEndedAction = {}

    fun start(onEnded: () -> Unit) {
        onEndedAction = onEnded
        startBattle()
    }

    fun end() {
        for (enemy in enemies) {
            if (enemy.isAlive) {
                enemy.die()
            }
        }
        destroyBattle()
        onEndedAction()
    }

    abstract fun destroyBattle()

    protected abstract fun startBattle()
}