package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.battle.BattleCanvas
import dev.zorsh.zorshDeltarune.battle.player.playerActionSelection.ActionSelectionButtonStage

class PlayerActionSelector(val canvas: BattleCanvas, val player: DeltarunePlayer) {
    interface ActionSelection {
        fun onLeftPressed(selector: PlayerActionSelector) {}
        fun onRightPressed(selector: PlayerActionSelector) {}
        fun onForwardPressed(selector: PlayerActionSelector) {}
        fun onBackwardPressed(selector: PlayerActionSelector) {}
        fun onJumpPressed(selector: PlayerActionSelector) {}
        fun onSneakPressed(selector: PlayerActionSelector) {}
        fun onSprintPressed(selector: PlayerActionSelector) {}
        fun updateEnter(selector: PlayerActionSelector) {}
        fun updateExit(selector: PlayerActionSelector) {}
    }

    var currentStage : ActionSelection = ActionSelectionButtonStage.BUTTON_FIGHT

    fun onLeftPressed() { currentStage.onLeftPressed(this) }
    fun onRightPressed() { currentStage.onRightPressed(this) }
    fun onForwardPressed() { currentStage.onForwardPressed(this) }
    fun onBackwardPressed() { currentStage.onBackwardPressed(this) }
    fun onJumpPressed() { currentStage.onJumpPressed(this) }
    fun onSneakPressed() { currentStage.onSneakPressed(this) }
    fun onSprintPressed() { currentStage.onSprintPressed(this) }

    fun startUpdate() {
        currentStage.updateEnter(this)
    }

    fun endUpdate() {
        currentStage.updateExit(this)
    }

    fun changeTo(new: ActionSelection) {
        currentStage.updateExit(this)
        currentStage = new
        currentStage.updateEnter(this)
    }
}