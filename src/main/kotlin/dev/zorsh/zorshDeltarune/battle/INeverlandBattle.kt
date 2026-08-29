package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.battle.enemy.DeltaruneEnemy
import dev.zorsh.zorshDeltarune.battle.player.DeltarunePlayer
import dev.zorsh.zorshDeltarune.battle.projectile.ProjectileData
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import java.util.UUID

interface INeverlandBattle {
    fun startBattle(onEnded: () -> Unit)
    fun endBattle()
    fun destroyBattle()

    fun isActive(): Boolean

    fun setUUID(uuid: UUID)

    fun getBattleInitialPlayers(): List<DeltarunePlayer>
    fun getBattlePlayers(): List<DeltarunePlayer>
    fun getBattleEnemies(): List<DeltaruneEnemy>

    fun createProjectile(px: Float, py: Float, projectileData: ProjectileData, afterCreated: (PlayerUICanvas, String) -> Unit)
}