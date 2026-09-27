package dev.zorsh.zorshDeltarune.battle.player.playerActionSelection

import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelector

enum class ActionSelectionAttackStage : PlayerActionSelector.ActionSelection {
    ATTACK_ENEMY {
        override fun updateExit(selector: PlayerActionSelector) {
            selector.canvas.removeAttackUI(selector.player.uuid)
        }
        override fun updateEnter(selector: PlayerActionSelector) {
            selector.player.player?.let { selector.canvas.startAttackUI(it) }
        }

        override fun onJumpPressed(selector: PlayerActionSelector) {
            selector.canvas.confirmAttack(selector.player.uuid)
            selector.changeTo(PlayerActionSelector.FinishedAction.END)
        }
    }
}