package dev.zorsh.zorshDeltarune.utils

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.round

fun Component.color(hex: String) = color(TextColor.fromHexString(hex))
fun Component.font(font: String) = font(Key.key(font))

fun coloredText(text: String, color: String) = Component.text(text).color(color)

fun fontText(text: String, color: String, font: String) = Component.text(text).color(color).font(font)

fun ghostEffect(text: Component, repeats: Int, angle: String): Component {
    val adder = Component.text("\uF802").font(Key.key("space:default"))
    var t = text.color("#fd64ff").append(adder)
    repeat(repeats) { i ->
        val n = i * round(20.0 / repeats) + 100
        val hex = Integer.toHexString(n.toInt())
        t = t.append(text.color("#fd$hex$angle").append(adder))
    }
    return t
}