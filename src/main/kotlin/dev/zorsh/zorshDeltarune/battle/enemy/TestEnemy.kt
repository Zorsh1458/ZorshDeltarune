package dev.zorsh.zorshDeltarune.battle.enemy

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.battle.projectile.CircleHitbox
import dev.zorsh.zorshDeltarune.battle.projectile.ProjectileData
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import java.lang.Math.clamp
import kotlin.math.cos
import kotlin.math.sin

class TestEnemy(
    name: Component,
    hitpoints: Int,
) : SpritedEnemy(
    name,
    hitpoints,
    listOf(
        Component.text("Это что еще за балбес"),
        Component.text("Тестовый враг встал у вас на пути!"),
        Component.text("Полный скебоб..")
    ),
    listOf(
        CanvasSprite.SLIME_SPRITE_1,
        CanvasSprite.SLIME_SPRITE_2
    ),
    6
) {

    override suspend fun attack(onAttackEnds: () -> Unit) = coroutineScope {
        val count = 60
        var yOffset = 0f
        repeat(count) { _ ->
            yOffset += ZorshDeltarune.random.nextFloat() * 16 - 8
            yOffset = clamp(yOffset, -64f, 64f)
            attackPattern2(yOffset)
            delay(100)
        }
        delay(6000)
    }

    fun attackPattern1(initialAngle: Float) {
        val (bbx, bby) = myBattle.getBBLocation()
        var radius = 100
        var angle = initialAngle
        val inx = cos(angle) * radius + bbx
        val iny = sin(angle) * radius + bby
        myBattle.createProjectile(
            inx, iny,
            ProjectileData(
                listOf(CanvasSprite.SOUL),
                null,
                CircleHitbox(8f)
            )
        ) { canvas, registryName, dealDamage ->
            runRepeating(120) { i ->
                if (i < 20) {
                    radius -= 2
                }
                val rad = radius + sin(i * 0.15f) * 10f
                angle -= 3.1415f * 0.03f
                val x = cos(angle) * rad + bbx
                val y = sin(angle) * rad + bby
                canvas.setPosition(x, y, registryName)
                if (i < 118) {
                    dealDamage(ZorshDeltarune.random.nextInt(10) + 10)
                }

                if (i == 117) {
                    canvas.setScale(0.5f, 0.5f, registryName)
                }
                if (i == 118) {
                    canvas.setScale(0f, 0f, registryName)
                }
            }
            runLater(121) {
                canvas.remove(registryName)
            }
        }
    }

    fun attackPattern2(yOffset: Float) {
        attackPattern2_projectile(yOffset + 32f)
        attackPattern2_projectile(yOffset + 64f)
        attackPattern2_projectile(yOffset - 32f)
        attackPattern2_projectile(yOffset - 64f)
    }

    fun attackPattern2_projectile(yOffset: Float) {
        val (bbx, bby) = myBattle.getBBLocation()
        myBattle.createProjectile(
            bbx + 256, bby + yOffset,
            ProjectileData(
                listOf(CanvasSprite.SQUARE),
                null,
                CircleHitbox(8f)
            )
        ) { canvas, registryName, dealDamage ->
            val count = 30
            runLater(2) {
                canvas.setScale(4f, 16f, registryName)
            }
            runRepeating(count) { i ->
                canvas.move(-20f, 0f, registryName)
                if (i < count-2) {
                    dealDamage(ZorshDeltarune.random.nextInt(10) + 10)
                }
            }
            runLater(-3L + count) {
                canvas.setScale(0.5f, 0.5f, registryName)
            }
            runLater(-2L + count) {
                canvas.setScale(0f, 0f, registryName)
            }
            runLater(1L + count) {
                canvas.remove(registryName)
            }
        }
        myBattle.createProjectile(
            bbx + 256, bby + yOffset,
            ProjectileData(
                listOf(CanvasSprite.SQUARE),
                null,
                CircleHitbox(8f)
            )
        ) { canvas, registryName, _ ->
            val count = 30
            runLater(1) {
                canvas.setZ(40, registryName)
                canvas.setSprite(CanvasSprite.SQUARE, ShaderTextColor.pure("#000000"), registryName)
            }
            runLater(2) {
                canvas.setScale(3f, 15f, registryName)
            }
            runRepeating(count) { i ->
                canvas.move(-20f, 0f, registryName)
            }
            runLater(-3L + count) {
                canvas.setScale(0.5f, 0.5f, registryName)
            }
            runLater(-2L + count) {
                canvas.setScale(0f, 0f, registryName)
            }
            runLater(1L + count) {
                canvas.remove(registryName)
            }
        }
    }
}