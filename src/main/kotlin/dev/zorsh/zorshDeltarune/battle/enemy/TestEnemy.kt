package dev.zorsh.zorshDeltarune.battle.enemy

import dev.zorsh.zorshDeltarune.battle.projectile.CircleHitbox
import dev.zorsh.zorshDeltarune.battle.projectile.ProjectileData
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component

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
        attackPattern1()
    }

    suspend fun attackPattern1() {
        myBattle.createProjectile(
            0f, 120f,
            ProjectileData(
                10,
                listOf(CanvasSprite.SOUL),
                null,
                CircleHitbox(5f)
            )
        ) { canvas, registryName, dealDamage ->
            runRepeating(15) {
                canvas.move(0f, -4f, registryName)
            }
            runRepeating(120) {
                canvas.rotate(3.1415f / 5f, registryName)
                dealDamage()
            }
            runLater(121) {
                canvas.remove(registryName)
            }
        }
        delay(6000)
    }
}