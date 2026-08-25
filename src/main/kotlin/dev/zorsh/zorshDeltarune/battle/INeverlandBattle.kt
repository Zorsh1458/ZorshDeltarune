package dev.zorsh.zorshDeltarune.battle

import java.util.UUID

interface INeverlandBattle {
    fun startBattle(onEnded: () -> Unit)
    fun endBattle()
    fun destroyBattle()

    fun setBattleUUID(uuid: UUID)

    fun getPlayers(): List<DeltarunePlayer>
    fun getEnemies(): List<DeltaruneEnemy>
}