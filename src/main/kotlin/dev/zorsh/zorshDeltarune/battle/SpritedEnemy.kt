package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import net.kyori.adventure.text.Component

abstract class SpritedEnemy(
    name: Component,
    hitpoints: Int,
    encounterMessages: List<Component>,
    val canvasSprites: List<CanvasSprite>,
    val framesPerSprite: Int
) : DeltaruneEnemy(
    name,
    hitpoints,
    encounterMessages
)