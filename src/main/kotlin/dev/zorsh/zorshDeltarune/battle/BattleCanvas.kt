package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay

class BattleCanvas(val players: List<Player>, val battle: INeverlandBattle) {
    val myCanvas = PlayerUICanvas()

    fun initCanvas() {
        myCanvas.initialize(players)
    }

    fun closeBattleBox() {
        val scale = myCanvas.getScale("battle_box_inner")
        runRepeating(10) { t ->
            val i = 9 - t
            myCanvas.setScale(i * 0.1f * scale.first, i * 0.1f * scale.second, "battle_box_inner")
            myCanvas.setScale(i * 0.1f * scale.first + 2, i * 0.1f * scale.second + 2, "battle_box_outer")
        }
        runLater(12) {
            myCanvas.remove("battle_box_inner")
            myCanvas.remove("battle_box_outer")
        }
    }

    fun openBattleBox(px: Float, py: Float, sx: Float, sy: Float) {
        myCanvas.drawSprite(
            px,
            py,
            0f,
            0f,
            60,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#00c400"),
            "battle_box_outer"
        )

        myCanvas.drawSprite(
            px,
            py,
            0f,
            0f,
            59,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000"),
            "battle_box_inner"
        )

        runRepeating(10) { t ->
            val i = t + 1
            myCanvas.setScale(i * 0.1f * sx, i * 0.1f * sy, "battle_box_inner")
            myCanvas.setScale(i * 0.1f * sx + 2, i * 0.1f * sy + 2, "battle_box_outer")
        }
    }

    fun setupLayout() {
        val spritedEnemies = battle.getBattleEnemies().filterIsInstance<SpritedEnemy>()
        for (enemy in spritedEnemies.reversed().withIndex()) {
            myCanvas.drawSprite(
                320f,
                20f - (spritedEnemies.size - 1) * 24f + enemy.index * 72f,
                0f,
                0f,
                16,
                enemy.value.canvasSprite,
                ShaderTextColor.pure("#ffffff"),
                "enemy_${enemy.index}"
            ) {
                runLater(10 + enemy.index.toLong() * 1L) {
                    runRepeating(10) { t ->
                        val i = t + 1
                        myCanvas.setScale(i / 10f, i / 10f, "enemy_${enemy.index}")
                        myCanvas.move(-20f + i * 2f, 0f, "enemy_${enemy.index}")
                    }
                }
            }
        }

        myCanvas.drawSprite(
            790f,
            0f,
            800f,
            800f,
            -2,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000"),
            "fg_slider_1"
        ) {
            runRepeating(30) { i ->
                if (i > 19) {
                    val t = i - 19
                    myCanvas.move(160f - t * 16f, 0f, "fg_slider_1")
                }
            }
            runLater(31) {
                myCanvas.remove("fg_slider_1")
            }
        }

        myCanvas.drawSprite(
            -790f,
            0f,
            800f,
            800f,
            -2,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000"),
            "fg_slider_2"
        ) {
            runRepeating(30) { i ->
                if (i > 19) {
                    val t = i - 19
                    myCanvas.move(-160f + t * 16f, 0f, "fg_slider_2")
                }
            }
            runLater(31) {
                myCanvas.remove("fg_slider_2")
            }
        }

        myCanvas.drawSprite(
            790f,
            0f,
            801f,
            800f,
            -1,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#ffffff"),
            "fg_slider_3"
        ) {
            runRepeating(30) { i ->
                if (i > 19) {
                    val t = i - 19
                    myCanvas.move(160f - t * 16f, 0f, "fg_slider_3")
                }
            }
            runLater(31) {
                myCanvas.remove("fg_slider_3")
            }
        }

        myCanvas.drawSprite(
            -790f,
            0f,
            801f,
            800f,
            -1,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#ffffff"),
            "fg_slider_4"
        ) {
            runRepeating(30) { i ->
                if (i > 19) {
                    val t = i - 19
                    myCanvas.move(-160f + t * 16f, 0f, "fg_slider_4")
                }
            }
            runLater(31) {
                myCanvas.remove("fg_slider_4")
            }
        }

        myCanvas.drawSprite(
            0f,
            0f,
            800f,
            800f,
            128,
            CanvasSprite.SQUARE,
            ShaderTextColor.effect(1, 4, 0)
        )

        myCanvas.drawSprite(
            0f,
            -478f,
            800f,
            400f,
            64,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000")
        )

        myCanvas.drawSprite(
            0f,
            -78f,
            800f,
            1f,
            32,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#2e1e25")
        )

        myCanvas.drawSprite(
            0f,
            -114f,
            800f,
            1f,
            32,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#2e1e25")
        )

        val dPlayers = battle.getBattlePlayers().filter { it.player != null && it.player?.isOnline == true }
        for (dPlayer in dPlayers) {
            myCanvas.drawSprite(
                0f,
                -96f,
                100f,
                19f,
                60,
                CanvasSprite.SQUARE,
                ShaderTextColor.pure("#00ffff"),
                null,
                dPlayer.player!!
            )

            myCanvas.drawSprite(
                0f,
                -96f,
                98f,
                17f,
                59,
                CanvasSprite.SQUARE,
                ShaderTextColor.pure("#000000"),
                null,
                dPlayer.player!!
            )

            myCanvas.drawText(
                -40f,
                -92f,
                1f,
                1f,
                58,
                Component.text("                       \n${dPlayer.player!!.name}"),
                ShaderTextColor.pure("#ffffff"),
                TextDisplay.TextAlignment.LEFT,
                1000,
                null,
                dPlayer.player!!
            )
        }
    }
}