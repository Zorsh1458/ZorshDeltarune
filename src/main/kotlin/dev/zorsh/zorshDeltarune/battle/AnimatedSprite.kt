package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.nms.PacketManager
import dev.zorsh.zorshDeltarune.utils.FakeDisplayData
import dev.zorsh.zorshDeltarune.utils.runInfinite
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

open class AnimatedSprite(
    var display: FakeTextDisplay,
    private var sprites: List<Component>,
    private var delay: Long
) {
    companion object {
        @JvmStatic
        fun create(location: Location, players: List<Player>, sprites: List<Component>, delay: Long, afterCreated: (AnimatedSprite) -> Unit) {
            PacketManager.spawnTextDisplay(
                location = location,
                text = sprites[0],
                players = players,
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(0f),
                        AxisAngle4f(),
                        Vector3f(1f),
                        AxisAngle4f()
                    )
                ),
                seeThrough = false,
                alignment = TextDisplay.TextAlignment.CENTER,
                isShadowed = true,
                lineWidth = 10000
            ) { display ->
                afterCreated(AnimatedSprite(display, sprites, delay))
            }
        }
    }

    init {
        startAnimation()
    }

    private var frame = 0
    private var exists = true

    private fun startAnimation() {
        runInfinite(delay) { _, task ->
            if (!exists || !display.exists) {
                task.cancel()
            } else {
                frame++
                if (frame >= sprites.size) {
                    frame = 0
                }
//                Bukkit.broadcast(Component.text("Animating frame $frame"))
                display.changeTransformation(display.transformation,
                    sprites[frame],
                    display.opacity
                )
            }
        }
    }

    fun destroy() {
        display.destroy()
        exists = false
    }
}