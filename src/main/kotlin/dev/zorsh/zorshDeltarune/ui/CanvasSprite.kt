package dev.zorsh.zorshDeltarune.ui

import dev.zorsh.zorshDeltarune.utils.font
import net.kyori.adventure.text.Component

enum class CanvasSprite {
    SQUARE,
    DBUTTON_FIGHT,
    DBUTTON_ACT,
    DBUTTON_ITEM,
    DBUTTON_MERCY,
    DBUTTON_DEFEND,
    UNKNOWN;

    fun isSpriteWidthEven() = when (this) {
        SQUARE -> true
        DBUTTON_FIGHT -> false
        DBUTTON_ACT -> false
        DBUTTON_ITEM -> false
        DBUTTON_MERCY -> false
        DBUTTON_DEFEND -> false
        else -> true
    }

    fun toTextValue(): Component {
        var text = when(this) {
            SQUARE -> "\uF002"
            DBUTTON_FIGHT -> "\uF003"
            DBUTTON_ACT -> "\uF004"
            DBUTTON_ITEM -> "\uF005"
            DBUTTON_MERCY -> "\uF006"
            DBUTTON_DEFEND -> "\uF007"
            else -> "\uF000"
        }
        if (isSpriteWidthEven()) {
            text += '\uF001'
        }
        return Component.text(text).font("space:ui")
    }
}