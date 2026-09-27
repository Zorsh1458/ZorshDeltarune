package dev.zorsh.zorshDeltarune.battle.player.playerActionSelection

import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelector

enum class ActionSelectionOptionsStage : PlayerActionSelector.ActionSelection {
    ENEMIES_FIGHT {
        override fun onSneakPressed(selector: PlayerActionSelector) {
            selector.changeTo(ActionSelectionButtonStage.BUTTON_FIGHT)
        }

        override fun onJumpPressed(selector: PlayerActionSelector) {
            selector.changeTo(ActionSelectionAttackStage.ATTACK_ENEMY)
        }

        override fun onLeftPressed(selector: PlayerActionSelector) {
            selector.canvas.moveOptionsSelectorLeft(selector.player.uuid)
        }
        override fun onRightPressed(selector: PlayerActionSelector) {
            selector.canvas.moveOptionsSelectorRight(selector.player.uuid)
        }
        override fun onForwardPressed(selector: PlayerActionSelector) {
            selector.canvas.moveOptionsSelectorUp(selector.player.uuid)
        }
        override fun onBackwardPressed(selector: PlayerActionSelector) {
            selector.canvas.moveOptionsSelectorDown(selector.player.uuid)
        }

        override fun updateExit(selector: PlayerActionSelector) {
            selector.canvas.clearEnemiesList(selector.player.uuid)
            selector.canvas.removeOptionsSelector(selector.player.uuid)
        }
        override fun updateEnter(selector: PlayerActionSelector) {
            selector.player.player?.let { selector.canvas.createEnemiesList(it) }
            selector.player.player?.let { selector.canvas.createOptionsSelector(it) }
        }
    }
}