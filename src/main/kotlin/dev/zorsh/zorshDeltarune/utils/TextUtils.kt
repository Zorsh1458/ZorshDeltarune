package dev.zorsh.zorshDeltarune.utils

import org.bukkit.ChatColor
import kotlin.math.max

//fun getWidth(str: String): Int {
//    return getWidth(false, str)
//}
//
//fun getWidth(wasBold: Boolean, str: String): Int {
//    var maxWidth = 0
//    var total = 0
//    val rawChars = str.toCharArray()
//    var i = 0
//    while (i < rawChars.size) {
//        val c = rawChars[i]
//        if (c == ChatColor.COLOR_CHAR && (i + 1) < rawChars.size) {
//            val c2 = rawChars[i + 1]
//            if (c2 == '[') {
//                while (i < rawChars.size && rawChars[i] != ']') {
//                    i++
//                }
//                i++
//                continue
//            }
//            i++
//            i++
//            continue
//        }
//        total += getWidth(c.toBoolean()) + (if (wasBold) 1 else 0)
//        if (c == '\n') {
//            if (total > maxWidth) {
//                maxWidth = total
//            }
//            total = 0
//        }
//        i++
//    }
//    return max(total.toDouble(), maxWidth.toDouble()).toInt()
//}