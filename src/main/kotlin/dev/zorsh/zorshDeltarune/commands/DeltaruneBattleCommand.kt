package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.battle.DefaultBattle
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import dev.zorsh.zorshDeltarune.battle.TestEnemy
import dev.zorsh.zorshDeltarune.nms.PacketManager
//import dev.zorsh.zorshDeltarune.nms.PacketManager
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

//            PacketManager.setAttribute(
//                net.minecraft.world.entity.ai.attributes.Attributes.JUMP_STRENGTH,
//                args.getOrElse(0) { "0.42" }.toDouble(),
//                sender.entityId,
//                listOf(sender)
//            )

//            PacketManager.setTickRate(
//                args.getOrElse(0) { "20" }.toFloat(),
//                args.getOrElse(1) { "false" }.toBoolean(),
//                Bukkit.getOnlinePlayers().toList()
//            )

//            if (args.isEmpty()) {
//                for (packet in PacketType.values().toList()) {
//                    sender.sendMessage(packet.name())
//                }
//            }

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