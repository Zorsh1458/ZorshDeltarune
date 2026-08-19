package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class NeverlandTestCommand: CommandExecutor, TabCompleter {
    override fun onCommand(
        player: CommandSender,
        p1: Command,
        p2: String,
        args: Array<out String>
    ): Boolean {
        if (player !is Player) return true

        try {
            if (args[1] == "canvas") {
                if (args[2] == "initialize") {
                    ZorshDeltarune.UIManager.initCanvas(player)
                }
                if (args[2] == "drawRect") {
                    val sx = args[3].toFloat()
                    val sy = args[4].toFloat()
                    val dx = args[5].toFloat()
                    val dy = args[6].toFloat()
                    val color = args[7]
                    ZorshDeltarune.UIManager.getCanvas(player)?.drawRect(sx, sy, dx, dy, color)
                }
                if (args[2] == "clear") {
                    ZorshDeltarune.UIManager.getCanvas(player)?.clear()
                }
            }
        } catch (e: Exception) {
            player.sendMessage(Component.text("ERROR: " + (e.message ?: "Неизвестная ошибка")).color(NamedTextColor.RED))
        }

        return true
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String?> {
        return when (p3.size) {
            0 -> emptyList()
            1 -> listOf("canvas")
            2 -> listOf("initialize", "drawRect", "clear")
            3 -> listOf("sx")
            4 -> listOf("sy")
            5 -> listOf("dx")
            6 -> listOf("dy")
            7 -> listOf("color (hex)", "#ff0000")
            else -> emptyList()
        }
    }
}