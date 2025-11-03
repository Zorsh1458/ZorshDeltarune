package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.BattleManager
import dev.zorsh.zorshDeltarune.battle.DefaultBattle
import dev.zorsh.zorshDeltarune.battle.DeltarunePlayer
import dev.zorsh.zorshDeltarune.battle.TestEnemy
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.fontText
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.sin

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
            PacketManager.spawnTextDisplay(
                sender.location,
                fontText("\uE201-", "#ffffff", "space:dsprites"),
                listOf(sender),
                FakeDisplayData(Transformation(
                    Vector3f(0f),
                    AxisAngle4f(),
                    Vector3f(15f),
                    AxisAngle4f()
                ))
            ) { entity ->
                runRepeating(200) { i ->
                    sender.sendMessage("Actual: ${abs(sin(i.toDouble() * 0.5) * 127).toInt()}")
                    entity.changeTransformation(entity.transformation, abs(sin(i.toDouble() * 0.5) * 127).toInt().toByte())
                }
                runLater(200) {
                    entity.destroy()
                }
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