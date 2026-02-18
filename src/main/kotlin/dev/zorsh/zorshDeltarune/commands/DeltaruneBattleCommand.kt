package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player


class DeltaruneBattleCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (ZorshDeltarune.getDPlayer(sender.uniqueId)?.locked != true) {
                val dPlayer = DeltarunePlayer(sender.uniqueId)
                ZorshDeltarune.deltarunePlayer[sender.uniqueId] = dPlayer
                val battle = DefaultBattle(
                    listOf(
                        dPlayer
                    ), listOf(
                        TestEnemy(100)
                    )
                )
                BattleManager.startBattle(battle)
            }

//            AnimatedSprite.create(
//                sender.location,
//                Bukkit.getOnlinePlayers().toList(),
//                listOf(
//                    fontText("1", "#ffffff", "space:enemy_slime"),
//                    fontText("2", "#ffffff", "space:enemy_slime")
//                ),
//                2L
//            ) { sprite ->
//                runLater(60) {
//                    sprite.destroy()
//                }
//            }
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