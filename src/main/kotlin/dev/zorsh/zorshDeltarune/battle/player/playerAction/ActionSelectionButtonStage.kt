package dev.zorsh.zorshDeltarune.battle.player.playerAction

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.BattleCanvas
import dev.zorsh.zorshDeltarune.battle.player.PlayerActionSelection

enum class ActionSelectionButtonStage : PlayerActionSelection {
    BUTTON_ATTACK {
        override fun onLeftPressed() = this
        override fun onRightPressed(): PlayerActionSelection {
            return BUTTON_ACT.withCanvas(canvas)
        }
        override fun onForwardPressed() = this
        override fun onBackwardPressed() = this
        override fun onJumpPressed() = this
        override fun onSneakPressed() = this
        override fun onSprintPressed() = this
    },
    BUTTON_ACT {
        override fun onLeftPressed(): PlayerActionSelection {
            return BUTTON_ATTACK.withCanvas(canvas)
        }
        override fun onRightPressed(): PlayerActionSelection {
            return BUTTON_ITEM.withCanvas(canvas)
        }
        override fun onForwardPressed() = this
        override fun onBackwardPressed() = this
        override fun onJumpPressed() = this
        override fun onSneakPressed() = this
        override fun onSprintPressed() = this
    },
    BUTTON_ITEM {
        override fun onLeftPressed(): PlayerActionSelection {
            return BUTTON_ACT.withCanvas(canvas)
        }
        override fun onRightPressed(): PlayerActionSelection {
            return BUTTON_MERCY.withCanvas(canvas)
        }
        override fun onForwardPressed() = this
        override fun onBackwardPressed() = this
        override fun onJumpPressed() = this
        override fun onSneakPressed() = this
        override fun onSprintPressed() = this
    },
    BUTTON_MERCY {
        override fun onLeftPressed(): PlayerActionSelection {
            return BUTTON_ITEM.withCanvas(canvas)
        }
        override fun onRightPressed(): PlayerActionSelection {
            return BUTTON_DEFEND.withCanvas(canvas)
        }
        override fun onForwardPressed() = this
        override fun onBackwardPressed() = this
        override fun onJumpPressed() = this
        override fun onSneakPressed() = this
        override fun onSprintPressed() = this
    },
    BUTTON_DEFEND {
        override fun onLeftPressed(): PlayerActionSelection {
            return BUTTON_MERCY.withCanvas(canvas)
        }
        override fun onRightPressed() = this
        override fun onForwardPressed() = this
        override fun onBackwardPressed() = this
        override fun onJumpPressed() = this
        override fun onSneakPressed() = this
        override fun onSprintPressed() = this
    };

    lateinit var canvas: BattleCanvas
    override fun withCanvas(newCanvas: BattleCanvas): PlayerActionSelection {
        canvas = newCanvas
        return this
    }

    override fun onJumpPressed(): PlayerActionSelection {
        ZorshDeltarune.instance.logger.info("Pressed jump from ActionSelectionButtonStage")
        canvas.setDebugText(name)
        return this
    }
}