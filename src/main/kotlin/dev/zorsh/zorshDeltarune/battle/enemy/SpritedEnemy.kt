package dev.zorsh.zorshDeltarune.battle.enemy

import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import net.kyori.adventure.text.Component

abstract class SpritedEnemy(
    name: String,
    maxHitpoints: Int,
    encounterMessages: List<Component>,
    val canvasSprites: List<CanvasSprite>,
    val framesPerSprite: Int
) : DeltaruneEnemy(
    name,
    maxHitpoints,
    encounterMessages
)