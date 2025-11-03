package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

class TestEnemy(hitpoints: Int) : DeltaruneEnemy(hitpoints) {

    override suspend fun attack(onAttackEnds: () -> Unit) = coroutineScope {
        val razdel = ZorshDeltarune.random.nextInt(4) + 1
        repeat(40) { angle ->
            launch {
                testSpawnNMS(angle, razdel)
            }
            delay(50)
        }
        delay(1500)
    }

    private fun testSpawnNMS(angle: Int, razdel: Int) {
        val a = angle.toDouble() * (6.283 / 40 / razdel + 6.283 / razdel)
        val primary = Vector3d(cos(a), sin(a), 0.0)
        val loc = projectileCenterLocation + primary * 2.0
        loc.yaw = 180f
        myBattle.newTextDisplay(
            loc,
            Component.text("⏺"),
            data = FakeDisplayData(Transformation(
                Vector3f(0f),
                AxisAngle4f(),
                Vector3f(0f),
                AxisAngle4f()
            ), teleportDuration = 40
            )
        ) { entity ->
            runLater(1) {
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        AxisAngle4f(a.toFloat() * -1f, 0f, 0f, 1f),
                        Vector3f(2f, 1.5f, 1f),
                        AxisAngle4f()
                    )
                )
            }
            var speed = 0.5
            var dest = loc
            runRepeating(42) { i ->
                dest += primary * speed
                entity.teleport(dest)
                speed -= 0.06
                if (i < 38) {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation,
                            entity.transformation.leftRotation,
                            Vector3f(2f + i * 0.17f, 1.5f - i * 0.03f, 1f),
                            Quaternionf(AxisAngle4f())
                        ), 64.toByte()
//                        ), ((sin(i.toDouble() * 0.6) + 1) * 127).toInt().toByte()
                    )
                }
            }
            runLater(39) {
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(0f),
                        AxisAngle4f()
                    )
                )
            }
            runLater(42) {
                entity.destroy()
            }
//            runRepeating(30) { i ->
//                var scale = 2f + sin(i * 0.7f)
//                if (i >= 25) {
//                    scale = 0f
//                }
//                entity.changeTransformation(
//                    Transformation(
//                        entity.transformation.translation,
//                        Quaternionf(AxisAngle4f()),
//                        Vector3f(scale),
//                        Quaternionf(AxisAngle4f())
//                    )
//                )
//            }
        }
    }
}