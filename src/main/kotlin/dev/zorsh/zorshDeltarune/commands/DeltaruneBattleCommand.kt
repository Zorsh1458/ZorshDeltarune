package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.joml.Vector2d

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
//            val hitbox = Hitbox(
//                Vector2d(0.0, -2.0),
//                listOf(
//                    Vector2d(-1.0, -1.0) to Vector2d(-1.0, 1.0),
//                    Vector2d(-1.0, 1.0) to Vector2d(1.0, 1.0),
//                    Vector2d(1.0, 1.0) to Vector2d(1.0, -1.0),
//                    Vector2d(1.0, -1.0) to Vector2d(-1.0, -1.0)
//                )
//            )
//            sender.sendMessage("[0,0] is: ${hitbox.isIn(Vector2d(0.0, 0.0))}")
//            sender.sendMessage("[0,-2] is: ${hitbox.isIn(Vector2d(0.0, -2.0))}")
//            sender.sendMessage("[0,-0.5] is: ${hitbox.isIn(Vector2d(0.0, -0.5))}")
//            sender.sendMessage("[0.5,0.5] is: ${hitbox.isIn(Vector2d(0.5, 0.5))}")
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