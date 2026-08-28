package dev.zorsh.zorshDeltarune.ui

import net.kyori.adventure.text.format.TextColor

class ShaderTextColor {
    val value: TextColor

    private constructor(v: TextColor) {
        value = v
    }

    companion object {
        @JvmStatic
        fun pure(hex: String): ShaderTextColor {
            val color = TextColor.fromHexString(hex) ?: return ShaderTextColor(TextColor.color(0f, 0f, 0f))
            val final = TextColor.color(color.red() - color.red() % 2, color.green(), color.blue())
            return ShaderTextColor(color)
        }

        @JvmStatic
        fun effect(effectClass: Int, effectId: Int, effectParameter: Int): ShaderTextColor {
            return ShaderTextColor(TextColor.color(effectClass, effectParameter, effectId))
        }
    }
}