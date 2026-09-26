package dev.zorsh.zorshDeltarune.battle.projectile

import dev.zorsh.zorshDeltarune.ui.CanvasSprite

data class ProjectileData(
    val sprites: List<CanvasSprite>,
    val framesPerSprite: Int?,
    val hitbox: Hitbox
)