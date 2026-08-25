package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import net.kyori.adventure.text.Component

abstract class SpritedEnemy(
    name: Component,
    hitpoints: Int,
    encounterMessages: List<Component>,
    val canvasSprite: CanvasSprite
) : DeltaruneEnemy(
    name,
    hitpoints,
    encounterMessages
)