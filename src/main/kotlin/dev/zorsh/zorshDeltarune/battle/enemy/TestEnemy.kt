package dev.zorsh.zorshDeltarune.battle.enemy

import dev.zorsh.zorshDeltarune.battle.projectile.CircleHitbox
import dev.zorsh.zorshDeltarune.battle.projectile.ProjectileData
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
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
        val count = 12
        repeat(count) { i ->
            attackPattern1(i * 6.283f / count)
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
                10,
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
                dealDamage()

                if (i == 119) {
                    canvas.setScale(0.5f, 0.5f, registryName)
                }
                if (i == 120) {
                    canvas.setScale(0f, 0f, registryName)
                }
            }
            runLater(121) {
                canvas.remove(registryName)
            }
        }
    }
}