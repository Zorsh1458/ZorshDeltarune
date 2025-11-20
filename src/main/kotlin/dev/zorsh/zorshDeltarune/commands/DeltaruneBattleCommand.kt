package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.tracker.EntityTracker
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player


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
            val loc = sender.location
            val ent = loc.world.spawnEntity(loc, EntityType.ARMOR_STAND)
            val tracker = BetterModel.limb("steve")
                .map { r -> r.getOrCreate(ent, sender) }
                .orElse(null)
            runRepeating(130) { tick ->
                if (tick % 13 == 0) {
                    tracker.animate("roll")
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