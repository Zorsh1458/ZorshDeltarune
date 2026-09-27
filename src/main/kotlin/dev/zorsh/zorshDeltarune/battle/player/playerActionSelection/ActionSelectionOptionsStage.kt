package dev.zorsh.zorshDeltarune.battle.player.playerActionSelection

import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelector

enum class ActionSelectionOptionsStage : PlayerActionSelector.ActionSelection {
    ENEMIES_FIGHT {
        override fun onSneakPressed(selector: PlayerActionSelector) {
            selector.changeTo(ActionSelectionButtonStage.BUTTON_FIGHT)
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