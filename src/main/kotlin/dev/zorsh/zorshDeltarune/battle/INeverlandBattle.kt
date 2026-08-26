package dev.zorsh.zorshDeltarune.battle

import java.util.UUID

interface INeverlandBattle {
    fun startBattle(onEnded: () -> Unit)
    fun endBattle()
    fun destroyBattle()

    fun setUUID(uuid: UUID)

    fun getBattleInitialPlayers(): List<DeltarunePlayer>
    fun getBattlePlayers(): List<DeltarunePlayer>
    fun getBattleEnemies(): List<DeltaruneEnemy>
}