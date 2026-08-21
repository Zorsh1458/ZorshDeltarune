package dev.zorsh.zorshDeltarune.commands

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.utils.runRepeating
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.round
import kotlin.math.sin

class NeverlandTestCommand : CommandExecutor, TabCompleter {
    override fun onCommand(
        player: CommandSender,
        p1: Command,
        p2: String,
        args: Array<out String>
    ): Boolean {
        if (player !is Player) return true

        try {
            if (args[0] == "canvas") {
                if (args[1] == "initialize") {
                    ZorshDeltarune.UIManager.initCanvas(player)
                }
                if (args[1] == "drawRect") {
                    val canvas = ZorshDeltarune.UIManager.getCanvas(player) ?: return true
                    val sx = args[2].toFloat()
                    val sy = args[3].toFloat()
                    val dx = args[4].toFloat()
                    val dy = args[5].toFloat()
                    val z = args[6].toInt()
                    val color = args[7]
                    canvas.drawRect(sx, sy, dx, dy, z, color)
                }
                if (args[1] == "drawSprite") {
                    val canvas = ZorshDeltarune.UIManager.getCanvas(player) ?: return true
                    val px = args[2].toFloat()
                    val py = args[3].toFloat()
                    val sx = args[4].toFloat()
                    val sy = args[5].toFloat()
                    val z = args[6].toInt()
                    val color = args[7]
                    val sprite = args[8]
                    val uuid = UUID.randomUUID()
                    canvas.drawSprite(
                        px, py, sx, sy, z,
                        CanvasSprite.valueOf(sprite.uppercase()), color,
                        "sprite_$uuid"
                    ) {
                        runRepeating(60) { i ->
                            canvas.move(0f, round(sin(i * 0.1f) * 4f) , "sprite_$uuid")
                        }
                    }
                }
                if (args[1] == "clear") {
                    ZorshDeltarune.UIManager.getCanvas(player)?.clear()
                }
            }
        } catch (e: Exception) {
            player.sendMessage(
                Component.text("ERROR: " + (e.message ?: "Неизвестная ошибка")).color(NamedTextColor.RED)
            )
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
            2 -> listOf("initialize", "drawRect", "drawSprite", "clear")
            3 -> listOf("px")
            4 -> listOf("py")
            5 -> listOf("dx")
            6 -> listOf("dy")
            7 -> listOf("z")
            8 -> listOf("color (hex)", "#ff0000")
            9 -> listOf("sprite")
            else -> emptyList()
        }
    }
}