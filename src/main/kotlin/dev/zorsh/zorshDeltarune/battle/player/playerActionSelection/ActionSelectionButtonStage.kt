package dev.zorsh.zorshDeltarune.battle.player.playerActionSelection

import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelector

enum class ActionSelectionButtonStage(val canvasObjName: String) : PlayerActionSelector.ActionSelection {
    BUTTON_FIGHT("player_button_fight") {
        override fun onRightPressed(selector: PlayerActionSelector) {
            selector.changeTo(BUTTON_ACT)
        }

        override fun onJumpPressed(selector: PlayerActionSelector) {
            selector.changeTo(ActionSelectionOptionsStage.ENEMIES_FIGHT)
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