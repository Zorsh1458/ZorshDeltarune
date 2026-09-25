package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.battle.BattleCanvas

// Naming:
// <phase: BUTTON|ENEMY|ACT|ITEM>_<state>

interface PlayerActionSelection {
    fun withCanvas(newCanvas: BattleCanvas): PlayerActionSelection

    fun onLeftPressed(): PlayerActionSelection
    fun onRightPressed(): PlayerActionSelection
    fun onForwardPressed(): PlayerActionSelection
    fun onBackwardPressed(): PlayerActionSelection
    fun onJumpPressed(): PlayerActionSelection
    fun onSneakPressed(): PlayerActionSelection
    fun onSprintPressed(): PlayerActionSelection
}