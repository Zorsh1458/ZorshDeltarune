package dev.zorsh.zorshDeltarune.utils

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

fun coloredText(text: String, color: String) = Component.text(text).color(TextColor.fromHexString(color))

fun fontText(text: String, color: String, font: String) = Component.text(text).color(TextColor.fromHexString(color)).font(Key.key(font))