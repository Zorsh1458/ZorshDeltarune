package dev.zorsh.zorshDeltarune.battle.player.playerAction

import dev.zorsh.zorshDeltarune.battle.BattleCanvas
import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelection

enum class ActionSelectionButtonStage : PlayerActionSelection {
    BUTTON_ATTACK {
        override fun onLeftPressed(canvas: BattleCanvas) = this
        override fun onRightPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_ACT
        }
        override fun onForwardPressed(canvas: BattleCanvas) = this
        override fun onBackwardPressed(canvas: BattleCanvas) = this
        override fun onSneakPressed(canvas: BattleCanvas) = this
        override fun onSprintPressed(canvas: BattleCanvas) = this
    },
    BUTTON_ACT {
        override fun onLeftPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_ATTACK
        }
        override fun onRightPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_ITEM
        }
        override fun onForwardPressed(canvas: BattleCanvas) = this
        override fun onBackwardPressed(canvas: BattleCanvas) = this
        override fun onSneakPressed(canvas: BattleCanvas) = this
        override fun onSprintPressed(canvas: BattleCanvas) = this
    },
    BUTTON_ITEM {
        override fun onLeftPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_ACT
        }
        override fun onRightPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_MERCY
        }
        override fun onForwardPressed(canvas: BattleCanvas) = this
        override fun onBackwardPressed(canvas: BattleCanvas) = this
        override fun onSneakPressed(canvas: BattleCanvas) = this
        override fun onSprintPressed(canvas: BattleCanvas) = this
    },
    BUTTON_MERCY {
        override fun onLeftPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_ITEM
        }
        override fun onRightPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_DEFEND
        }
        override fun onForwardPressed(canvas: BattleCanvas) = this
        override fun onBackwardPressed(canvas: BattleCanvas) = this
        override fun onSneakPressed(canvas: BattleCanvas) = this
        override fun onSprintPressed(canvas: BattleCanvas) = this
    },
    BUTTON_DEFEND {
        override fun onLeftPressed(canvas: BattleCanvas): PlayerActionSelection {
            return BUTTON_MERCY
        }
        override fun onRightPressed(canvas: BattleCanvas) = this
        override fun onForwardPressed(canvas: BattleCanvas) = this
        override fun onBackwardPressed(canvas: BattleCanvas) = this
        override fun onSneakPressed(canvas: BattleCanvas) = this
        override fun onSprintPressed(canvas: BattleCanvas) = this
    };

    override fun onJumpPressed(canvas: BattleCanvas): PlayerActionSelection {
        canvas.setDebugText(name)
        return this
    }
}