package dev.zorsh.zorshDeltarune.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class NeverlandTestCommand: CommandExecutor, TabCompleter {
    override fun onCommand(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): Boolean {
        return false
    }

    override fun onTabComplete(
        p0: CommandSender,
        p1: Command,
        p2: String,
        p3: Array<out String>
    ): List<String?> {
        return when (p3.size) {
            0 -> emptyList()
            1 -> listOf("test1")
            2 -> listOf("test2")
            3 -> listOf("test3", "aa", "fsfwefef sada sad as ")
            else -> emptyList()
        }
    }
}