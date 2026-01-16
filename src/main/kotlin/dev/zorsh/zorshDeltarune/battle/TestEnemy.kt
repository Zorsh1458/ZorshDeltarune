package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.util.Transformation
import org.joml.*
import kotlin.math.*
import kotlin.random.Random

class TestEnemy(
    hitpoints: Int
) : DeltaruneEnemy(hitpoints,
    listOf(
        Component.text("Это что еще за балбес"),
        Component.text("Тестовый враг встал у вас на пути!"),
        Component.text("Полный скебоб..")
    )
) {

    private var attackCount = 0

    override suspend fun attack(onAttackEnds: () -> Unit) = coroutineScope {
        attackCount++
        if (attackCount % 2 == 0) {
            repeat(20) { _ ->
                launch {
                    testSpawnNMS2()
                }
                delay(250)
            }
        } else {
            repeat(6) { _ ->
                launch {
                    attackVariant3()
                }
                delay(1200)
            }
        }
        delay(1500)
    }

    private fun testSpawnNMS2() {
        val scale = myBattle.sceneScale
        val loc = myBattle.battleCenterLocation
        loc.pitch = -90f
        myBattle.newProjectile(
            2,
            Vector3f(ZorshDeltarune.random.nextFloat() * 50f - 25f, -25f, 0.01f)
        ) { entity ->
            runLater(1) {
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(7f, 7f, 1f) * scale,
                        AxisAngle4f()
                    )
                )
            }
            var hit = false
            var speed = -2.8f
            runRepeating(70) { _, _ ->
                val t = entity.transformation.translation
                val cent = Vector3f(
                    -t.x,
                    t.z,
                    t.y
                )
                runLater(1) {
                    hit = hit || myBattle.damageHitbox(ZorshDeltarune.random.nextInt(10) + 20, cent, 0.1)
                }
                if (hit) {
                    entity.destroy()
                } else {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation - Vector3f(
                                0f,
                                speed,
                                0.0001f
                            ) * scale,
                            entity.transformation.leftRotation,
                            entity.transformation.scale,
                            Quaternionf(AxisAngle4f())
                        )
                    )
                    speed += 0.1f
                }
            }
            runLater(70) {
                if (!hit) {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation,
                            entity.transformation.leftRotation,
                            Vector3f(0f, 0f, 1f),
                            Quaternionf(AxisAngle4f())
                        )
                    )
                }
            }
            runLater(72) {
                if (!hit) {
                    entity.destroy()
                }
            }
        }
    }

    private fun attackVariant3() {
        val x = ZorshDeltarune.random.nextFloat() * 20f - 10f
        val y = ZorshDeltarune.random.nextFloat() * 20f - 10f
        val aOff = ZorshDeltarune.random.nextInt(24)
        repeat(8) { i ->
            testSpawnNMS3(i * 47 + aOff, x, y)
        }
    }

    private fun testSpawnNMS3(a: Int, centerX: Float, centerY: Float) {
        val scale = myBattle.sceneScale
        val loc = myBattle.battleCenterLocation
        loc.pitch = -90f
        val x = sin(a / 180.0 * 3.1415)
        val y = cos(a / 180.0 * 3.1415)
        myBattle.newProjectile(
            1,
            Vector3f(centerX + x.toFloat() * 60, centerY + y.toFloat() * 60, 0.01f)
        ) { entity ->
            runLater(1) {
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        AxisAngle4f(a / 180.0f * -3.1415f, 0f, 0f, 1f),
                        Vector3f(12f, 12f, 1f) * scale,
                        AxisAngle4f()
                    )
                )
            }
            var hit = false
            runRepeating(40) { i, _ ->
                val t = entity.transformation.translation
                val cent = Vector3f(
                    -t.x,
                    t.z,
                    t.y
                )
                runLater(1) {
                    hit = hit || myBattle.damageHitbox(ZorshDeltarune.random.nextInt(10) + 20, cent, 0.17)
                }
                if (hit) {
                    entity.destroy()
                } else {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation - Vector3f(
                                1.5f * x.toFloat(),
                                1.5f * y.toFloat(),
                                0.0001f
                            ) * scale,
                            entity.transformation.leftRotation,
                            entity.transformation.scale,
                            Quaternionf(AxisAngle4f())
                        )
                    )
                }
            }
            runLater(40) {
                if (!hit) {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation,
                            entity.transformation.leftRotation,
                            Vector3f(0f, 0f, 1f),
                            Quaternionf(AxisAngle4f())
                        )
                    )
                }
            }
            runLater(42) {
                if (!hit) {
                    entity.destroy()
                }
            }
        }
    }

    private fun testSpawnNMS(i: Int) {
        val scale = myBattle.sceneScale
        val loc = myBattle.battleCenterLocation
        loc.pitch = -90f
        val a = i * 3703 + ZorshDeltarune.random.nextInt(20) * 5
        val x = sin(a / 180.0 * 3.1415)
        val y = cos(a / 180.0 * 3.1415)
        myBattle.newProjectile(
            1,
            Vector3f(x.toFloat() * 30f, y.toFloat() * 30f, 0.01f)
        ) { entity ->
            runLater(1) {
                entity.changeTransformation(
                    Transformation(
                        entity.transformation.translation,
                        AxisAngle4f(a / 180.0f * -3.1415f, 0f, 0f, 1f),
                        Vector3f(12f, 12f, 1f) * scale,
                        AxisAngle4f()
                    )
                )
            }
            var hit = false
            runRepeating(40) { i, _ ->
                val t = entity.transformation.translation
                val cent = Vector3f(
                    -t.x,
                    t.z,
                    t.y
                )
                runLater(1) {
                    hit = hit || myBattle.damageHitbox(ZorshDeltarune.random.nextInt(10) + 20, cent, 0.17)
                }
                if (hit) {
                    entity.destroy()
                } else {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation - Vector3f(
                                1.5f * x.toFloat(),
                                1.5f * y.toFloat(),
                                0.0001f
                            ) * scale,
                            entity.transformation.leftRotation,
                            entity.transformation.scale,
                            Quaternionf(AxisAngle4f())
                        )
                    )
                }
            }
            runLater(40) {
                if (!hit) {
                    entity.changeTransformation(
                        Transformation(
                            entity.transformation.translation,
                            entity.transformation.leftRotation,
                            Vector3f(0f, 0f, 1f),
                            Quaternionf(AxisAngle4f())
                        )
                    )
                }
            }
            runLater(42) {
                if (!hit) {
                    entity.destroy()
                }
            }
        }
    }
}