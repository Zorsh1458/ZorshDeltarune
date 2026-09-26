package dev.zorsh.zorshDeltarune.ui

import dev.zorsh.zorshDeltarune.utils.font
import net.kyori.adventure.text.Component
import kotlin.math.max

enum class CanvasSprite(private val sizeX: Int, private val sizeY: Int, private val textValue: Char) {
    UNKNOWN                 (64, 32, '\uF000'),
    SQUARE                  (2, 2, '\uF002'),
    DBUTTON_FIGHT           (62, 52, '\uF003'),
    DBUTTON_ACT             (62, 52, '\uF004'),
    DBUTTON_ITEM            (62, 52, '\uF005'),
    DBUTTON_MERCY           (62, 52, '\uF006'),
    DBUTTON_DEFEND          (62, 52, '\uF007'),
    MOUSE                   (9, 9, '\uF009'),
    SLIME_SPRITE_1          (34, 28, '\uF00A'),
    SLIME_SPRITE_2          (30, 34, '\uF00B'),
    SOUL                    (20, 20, '\uF00C'),
    SOUL_OTHER              (20, 20, '\uF00D'),
    SOUL_OUTLINE            (40, 40, '\uF00E'),
    SOUL_OUTLINE_BIG        (48, 48, '\uF00F'),
    TP_BAR_OUTLINE          (18, 116, '\uF010'),
    DBUTTON_FIGHT_SELECTED  (62, 52, '\uF011'),
    DBUTTON_ACT_SELECTED    (62, 52, '\uF012'),
    DBUTTON_ITEM_SELECTED   (62, 52, '\uF013'),
    DBUTTON_MERCY_SELECTED  (62, 52, '\uF014'),
    DBUTTON_DEFEND_SELECTED (62, 52, '\uF015');

    fun applyScaling() = when (this) {
        SQUARE -> false
        else -> true
    }

    fun getSizeRatios(): Pair<Float, Float> {
        if (applyScaling()) {
            return sizeX.toFloat() / max(1, sizeX - 1) to sizeY.toFloat() / max(1, sizeY - 1)
        }
        return 1f to 1f
    }

    fun isSpriteWidthEven() = sizeX % 2 == 0

    fun toTextValue(): Component {
        var text = "$textValue"
        if (isSpriteWidthEven()) {
            text += '\uF001'
        }
        return Component.text(text).font("space:ui")
    }
}