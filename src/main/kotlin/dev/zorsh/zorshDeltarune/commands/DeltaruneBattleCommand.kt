package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player


class DeltaruneBattleCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player && sender.name == "Zorsh") {
            val dPlayers = args
                .mapNotNull { Bukkit.getPlayer(it) }
                .filter { ZorshDeltarune.getDPlayer(it.uniqueId)?.locked != true }
                .map {
                    val dPlayer = DeltarunePlayer(it.uniqueId)
                    ZorshDeltarune.deltarunePlayer[it.uniqueId] = dPlayer
                    dPlayer
                }
            if (dPlayers.isNotEmpty()) {
                val battle = NeverlandBattle(
                    dPlayers,
                    listOf(
                        TestEnemy(Component.text("Слизнячок"), 100),
                        TestEnemy(Component.text("Слизнячок"), 100),
                        TestEnemy(Component.text("Слизнячок"), 100)
                    )
                )
                BattleManager.startBattle(battle)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        return emptyList()
    }
}