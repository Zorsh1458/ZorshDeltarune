package dev.zorsh.zorshDeltarune.utils

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

fun Component.color(hex: String) = color(TextColor.fromHexString(hex))
fun Component.font(font: String) = font(Key.key(font))

fun fontText(text: String, color: String, font: String) = Component.text(text).color(color).font(font)
