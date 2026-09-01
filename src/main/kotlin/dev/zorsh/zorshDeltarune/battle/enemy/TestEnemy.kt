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
        val count = 16
        repeat(count) { i ->
            attackPattern1(i * 6.283f / count)
        }
    }

    suspend fun attackPattern1(initialAngle: Float) {
        val (bbx, bby) = myBattle.getBBLocation()
        myBattle.createProjectile(
            0f, 120f,
            ProjectileData(
                10,
                listOf(CanvasSprite.SOUL),
                null,
                CircleHitbox(8f)
            )
        ) { canvas, registryName, dealDamage ->
            var radius = 80
            var angle = initialAngle
            runRepeating(120) { i ->
                if (i < 10) {
                    radius -= 2
                }
                angle += 3.1415f * 0.05f
                val x = cos(angle) * radius + bbx
                val y = sin(angle) * radius + bby
                canvas.setPosition(x, y, registryName)
                dealDamage()
            }
            runLater(121) {
                canvas.remove(registryName)
            }
        }
        delay(6000)
    }
}