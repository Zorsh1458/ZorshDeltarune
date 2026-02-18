package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import net.kyori.adventure.text.Component

abstract class SpritedEnemy(
    hitpoints: Int,
    encounterMessages: List<Component>,
    private val sprites: List<Component>,
    private val delay: Long
) : DeltaruneEnemy(
    hitpoints,
    encounterMessages
) {
    lateinit var mySprite: AnimatedSprite

    open fun createSprite(display: FakeTextDisplay) {
        mySprite = AnimatedSprite(display, sprites, delay)
    }
}