package dev.zorsh.zorshDeltarune.battle.projectile

import dev.zorsh.zorshDeltarune.ui.CanvasSprite

data class ProjectileData(
    val damage: Int,
    val sprites: List<CanvasSprite>,
    val framesPerSprite: Int?,
    val hitbox: Hitbox
)