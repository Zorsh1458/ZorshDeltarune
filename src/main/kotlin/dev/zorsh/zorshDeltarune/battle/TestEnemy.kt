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
        repeat(160) {
            repeat(30) {
                launch {
                    testSpawnNMS()
                }
            }
            delay(50)
        }
    }

    private fun testSpawnNMS() {
        val primary = Vector3d(random.nextDouble() * 0.2 - 0.1, -0.2, 0.0)
        val loc = myBattle.battleBoxCenterLocation + Vector3d(random.nextDouble() * 5.0 - 2.5, 5.0 ,0.0)
        loc.yaw = 180f
        myBattle.newTextDisplay(
            loc,
            Component.text("⏺"),
        ) { entity ->
            CoroutineScope(Dispatchers.IO).launch {
                var i = 0
                repeat(20) {
                    i++
                    entity.teleport(entity.location + primary)
                    entity.changeTransformation(Transformation(
                        Vector3f(0f, 0f, 0f),
                        Quaternionf(AxisAngle4f()),
                        Vector3f(2f + sin(i * 0.7f)),
                        Quaternionf(AxisAngle4f())
                    ))
                    delay(50)
                }
                entity.destroy()
            }
        }
    }
}