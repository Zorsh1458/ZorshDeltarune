package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.sin
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    private val random = Random(100)

    override suspend fun attack(onAttackEnds: () -> Unit) = coroutineScope {
        repeat(80) {
            repeat(2) {
                launch {
                    testSpawnNMS()
                }
            }
            delay(100)
        }
        delay(1500)
    }

    private fun testSpawnNMS() {
        val primary = Vector3d(random.nextDouble() * 0.2 - 0.1, -0.2, 0.0)
        val loc = projectileCenterLocation + Vector3d(random.nextDouble() * 5.0 - 2.5, 5.0 ,0.0)
        loc.yaw = 180f
        myBattle.newTextDisplay(
            loc,
            Component.text("⏺"),
            data = FakeDisplayData(Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(0f),
                AxisAngle4f()
            ), teleportDuration = 30
            )
        ) { entity ->
            val dest = loc + primary * 30.0
            runRepeating(30) { i ->
                if (i < 2) {
                    entity.teleport(dest)
                }
                var scale = 2f + sin(i * 0.7f)
                if (i >= 25) {
                    scale = 0f
                }
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        Quaternionf(AxisAngle4f()),
                        Vector3f(scale),
                        Quaternionf(AxisAngle4f())
                    )
                )
                if (i == 29) {
                    entity.destroy()
                }
            }
        }
    }
}