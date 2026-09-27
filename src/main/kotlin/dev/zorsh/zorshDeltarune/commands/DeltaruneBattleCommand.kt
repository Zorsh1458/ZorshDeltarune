package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.*
import dev.zorsh.zorshDeltarune.battle.enemy.TestEnemy
import dev.zorsh.zorshDeltarune.battle.player.DeltarunePlayer
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
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
                    ZorshDeltarune.getDPlayer(it.uniqueId) ?: (DeltarunePlayer(it.uniqueId).also { dp -> ZorshDeltarune.deltarunePlayer[it.uniqueId] = dp })
                }
            if (dPlayers.isNotEmpty()) {
                val battle = NeverlandBattle(
                    dPlayers,
                    listOf(
                        TestEnemy("Зеленый слизень", 100, listOf(CanvasSprite.SLIME_GREEN_1, CanvasSprite.SLIME_GREEN_2)),
                        TestEnemy("Синий слизнячок", 100, listOf(CanvasSprite.SLIME_BLUE_1, CanvasSprite.SLIME_BLUE_2)),
                        TestEnemy("Красный слизенище", 100, listOf(CanvasSprite.SLIME_RED_1, CanvasSprite.SLIME_RED_2)),
                        TestEnemy("Желтый слизняк", 100, listOf(CanvasSprite.SLIME_YELLOW_1, CanvasSprite.SLIME_YELLOW_2))
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