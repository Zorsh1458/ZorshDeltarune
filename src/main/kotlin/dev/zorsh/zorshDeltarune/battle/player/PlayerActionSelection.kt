package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.battle.BattleCanvas

// Naming:
// <phase: BUTTON|ENEMY|ACT|ITEM>_<state>

interface PlayerActionSelection {
    fun onLeftPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onRightPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onForwardPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onBackwardPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onJumpPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onSneakPressed(canvas: BattleCanvas): PlayerActionSelection
    fun onSprintPressed(canvas: BattleCanvas): PlayerActionSelection
}