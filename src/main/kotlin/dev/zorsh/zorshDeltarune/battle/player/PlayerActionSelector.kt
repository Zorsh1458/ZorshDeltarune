package dev.zorsh.zorshDeltarune.battle.player

import dev.zorsh.zorshDeltarune.battle.BattleCanvas

// Naming:
// <phase: BUTTON|ENEMY|ACT|ITEM>_<state>

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

    enum class ActionSelectionButtonStage(val canvasObjName: String) : ActionSelection {
        BUTTON_FIGHT("player_button_fight") {
            override fun onRightPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_ACT)
            }
        },
        BUTTON_ACT("player_button_act") {
            override fun onLeftPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_FIGHT)
            }
            override fun onRightPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_ITEM)
            }
        },
        BUTTON_ITEM("player_button_item") {
            override fun onLeftPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_ACT)
            }
            override fun onRightPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_MERCY)
            }
        },
        BUTTON_MERCY("player_button_mercy") {
            override fun onLeftPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_ITEM)
            }
            override fun onRightPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_DEFEND)
            }
        },
        BUTTON_DEFEND("player_button_defend") {
            override fun onLeftPressed(selector: PlayerActionSelector) {
                selector.changeTo(BUTTON_MERCY)
            }
        };

        override fun updateExit(selector: PlayerActionSelector) {
            selector.canvas.removePlayerButtonSelection(canvasObjName, selector.player.uuid)
        }
        override fun updateEnter(selector: PlayerActionSelector) {
            selector.canvas.setPlayerButtonSelection(canvasObjName, selector.player.uuid)
        }
    }

    var currentStage : ActionSelection = ActionSelectionButtonStage.BUTTON_FIGHT

    fun onLeftPressed() { currentStage.onLeftPressed(this) }
    fun onRightPressed() { currentStage.onRightPressed(this) }
    fun onForwardPressed() { currentStage.onForwardPressed(this) }
    fun onBackwardPressed() { currentStage.onBackwardPressed(this) }
    fun onJumpPressed() { currentStage.onJumpPressed(this) }
    fun onSneakPressed() { currentStage.onSneakPressed(this) }
    fun onSprintPressed() { currentStage.onSprintPressed(this) }

    init {
        currentStage.updateEnter(this)
    }

    fun changeTo(new: ActionSelection) {
        currentStage.updateExit(this)
        currentStage = new
        currentStage.updateEnter(this)
    }
}