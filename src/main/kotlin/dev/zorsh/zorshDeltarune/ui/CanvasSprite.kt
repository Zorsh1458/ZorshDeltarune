package dev.zorsh.zorshDeltarune.ui

import dev.zorsh.zorshDeltarune.utils.font
import net.kyori.adventure.text.Component
import kotlin.math.max

enum class CanvasSprite {
    MOUSE,
    SQUARE,
    DBUTTON_FIGHT,
    DBUTTON_ACT,
    DBUTTON_ITEM,
    DBUTTON_MERCY,
    DBUTTON_DEFEND,
    SLIME_SPRITE_1,
    SLIME_SPRITE_2,
    SOUL,
    SOUL_OTHER,
    SOUL_OUTLINE,
    SOUL_OUTLINE_BIG,
    UNKNOWN;

    fun getSize() = when (this) {
        MOUSE -> 9 to 9
        SQUARE -> 2 to 2
        DBUTTON_FIGHT -> 31 to 26
        DBUTTON_ACT -> 31 to 26
        DBUTTON_ITEM -> 31 to 26
        DBUTTON_MERCY -> 31 to 26
        DBUTTON_DEFEND -> 31 to 26
        SLIME_SPRITE_1 -> 34 to 28
        SLIME_SPRITE_2 -> 30 to 34
        SOUL -> 20 to 20
        SOUL_OTHER -> 20 to 20
        UNKNOWN -> 64 to 32
        SOUL_OUTLINE -> 40 to 40
        SOUL_OUTLINE_BIG -> 48 to 48
    }

    fun applyScaling() = when (this) {
        SQUARE -> false
        else -> true
    }

    fun getSizeRatios(): Pair<Float, Float> {
        if (applyScaling()) {
            val size = getSize()
            return size.first.toFloat() / max(1, size.first - 1) to size.second.toFloat() / max(1, size.second - 1)
        }
        return 1f to 1f
    }

    fun isSpriteWidthEven() = getSize().first % 2 == 0

    fun toTextValue(): Component {
        var text = when(this) {
            MOUSE -> "\uF009"
            SQUARE -> "\uF002"
            DBUTTON_FIGHT -> "\uF003"
            DBUTTON_ACT -> "\uF004"
            DBUTTON_ITEM -> "\uF005"
            DBUTTON_MERCY -> "\uF006"
            DBUTTON_DEFEND -> "\uF007"
            SLIME_SPRITE_1 -> "\uF00A"
            SLIME_SPRITE_2 -> "\uF00B"
            SOUL -> "\uF00C"
            SOUL_OTHER -> "\uF00D"
            SOUL_OUTLINE -> "\uF00E"
            SOUL_OUTLINE_BIG -> "\uF00F"
            UNKNOWN -> "\uF000"
        }
        if (isSpriteWidthEven()) {
            text += '\uF001'
        }
        return Component.text(text).font("space:ui")
    }
}