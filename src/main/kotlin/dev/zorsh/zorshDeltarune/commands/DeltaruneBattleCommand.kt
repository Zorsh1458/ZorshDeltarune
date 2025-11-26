package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import dev.zorsh.zorshDeltarune.bettermodel.BattlePlayer
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runRepeating
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.joml.Vector3d


class DeltaruneBattleCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
//            if (ZorshDeltarune.getDPlayer(sender.uniqueId)?.locked != true) {
//                val dPlayer = DeltarunePlayer(sender.uniqueId)
//                ZorshDeltarune.deltarunePlayer[sender.uniqueId] = dPlayer
//                val battle = DefaultBattle(
//                    listOf(
//                        dPlayer
//                    ), listOf(
//                        TestEnemy(100)
//                    )
//                )
//                BattleManager.startBattle(battle)
//            }

            PacketManager.setShaderData(
                args.getOrElse(0) { "1" }.toLong(),
                listOf(sender),
                600
            )

//            sender.let {
//                val battlePlayer = BattlePlayer()
//                val modelLocation = sender.location + Vector3d(5.0, 0.75, 0.0)
//                modelLocation.yaw = 90f
//                battlePlayer.create(it, modelLocation)
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