package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.battle.enemy.SpritedEnemy
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.runInfinite
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import kr.toxicity.model.api.tracker.TrackerUpdateAction.brightness
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.util.UUID

class BattleCanvas(val players: List<Player>, val battle: INeverlandBattle) {
    val myCanvas = PlayerUICanvas()

    fun initCanvas() {
        myCanvas.initialize(players)
    }

    private fun section(name: String, active: Boolean, action: () -> Unit) {
        if (active) {
            action()
        }
    }

    fun closeBattleBox() {
        val scale = myCanvas.getScale("battle_box_inner")
        runRepeating(10) { t ->
            val i = 9 - t
            myCanvas.setScale(i * 0.1f * scale.first, i * 0.1f * scale.second, "battle_box_inner")
            myCanvas.setScale(i * 0.1f * scale.first + 2, i * 0.1f * scale.second + 2, "battle_box_outer")
            myCanvas.rotate(3.1415f / 10f, "battle_box_inner")
            myCanvas.rotate(3.1415f / 10f, "battle_box_outer")
        }
        runLater(12) {
            myCanvas.remove("battle_box_inner")
            myCanvas.remove("battle_box_outer")
        }
    }

    fun openBattleBox(px: Float, py: Float, sx: Float, sy: Float) {
        section("BATTLE_BOX", true) {
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
        }

        runRepeating(10) { t ->
            val i = t + 1
            myCanvas.setScale(i * 0.1f * sx, i * 0.1f * sy, "battle_box_inner")
            myCanvas.setScale(i * 0.1f * sx + 2, i * 0.1f * sy + 2, "battle_box_outer")
            myCanvas.rotate(3.1415f / 10f, "battle_box_inner")
            myCanvas.rotate(3.1415f / 10f, "battle_box_outer")
        }
    }

    fun showPlayerOptions() {
        val dPlayers = battle.getBattlePlayers()
        dPlayers.forEach { dPlayer ->
            playerOptionsObjectNamesToLift.forEach { objName ->
                myCanvas.move(0f, 36f, objName, dPlayer.player!!.uniqueId)
            }
        }
    }

    fun hidePlayerOptions() {
        val dPlayers = battle.getBattlePlayers()
        dPlayers.forEach { dPlayer ->
            playerOptionsObjectNamesToLift.forEach { objName ->
                myCanvas.move(0f, -36f, objName, dPlayer.player!!.uniqueId)
            }
        }
    }

    fun animateSprite(
        spriteList: List<CanvasSprite>,
        framesPerSprite: Int,
        objName: String,
        stoppingCondition: () -> Boolean,
    ) {
        runInfinite(1) { i, action ->
            if (stoppingCondition()) {
                action.cancel()
                return@runInfinite
            }

            val currentSprite = spriteList[(i / framesPerSprite) % spriteList.size]
            myCanvas.setSprite(
                currentSprite,
                ShaderTextColor.pure("#ffffff"),
                objName
            )
        }
    }

    fun updateHealthInfo(hp: Int, maxHp: Int, playerId: UUID) {
        val offsetPercentage = hp.toFloat() / maxHp.toFloat()
        val py = myCanvas.getPosition("player_hp_bar_green", playerId).second
        myCanvas.setPosition(35f + offsetPercentage * 28f, py, "player_hp_bar_green", playerId)
        myCanvas.setScale(offsetPercentage * 28f, 4f, "player_hp_bar_green", playerId)
        val hpCountText = Component.text("                       \n$hp / $maxHp")
        myCanvas.setText(hpCountText, "player_hp_counter", playerId)
    }

    fun setDebugText(text: String) {
        battle.getBattlePlayers().forEach { dPlayer ->
            myCanvas.setText(Component.text("Debug: $text"), "debug_info", dPlayer.uuid)
        }
    }

    fun setTurnTimeScale(scale: Float, playerId: UUID) {
        myCanvas.setScale(scale * 98, 1f, "selection_box_time_scale", playerId)
    }

    fun setPlayerButtonSelection(objName: String, playerUUID: UUID) {
        val col = ShaderTextColor.pure("#ffffff")
        when (objName) {
            "player_button_fight" -> myCanvas.setSprite(CanvasSprite.DBUTTON_FIGHT_SELECTED, col, objName, playerUUID)
            "player_button_act" -> myCanvas.setSprite(CanvasSprite.DBUTTON_ACT_SELECTED, col, objName, playerUUID)
            "player_button_item" -> myCanvas.setSprite(CanvasSprite.DBUTTON_ITEM_SELECTED, col, objName, playerUUID)
            "player_button_mercy" -> myCanvas.setSprite(CanvasSprite.DBUTTON_MERCY_SELECTED, col, objName, playerUUID)
            "player_button_defend" -> myCanvas.setSprite(CanvasSprite.DBUTTON_DEFEND_SELECTED, col, objName, playerUUID)
        }
    }

    fun removePlayerButtonSelection(objName: String, playerUUID: UUID) {
        val col = ShaderTextColor.pure("#ffffff")
        when (objName) {
            "player_button_fight" -> myCanvas.setSprite(CanvasSprite.DBUTTON_FIGHT, col, objName, playerUUID)
            "player_button_act" -> myCanvas.setSprite(CanvasSprite.DBUTTON_ACT, col, objName, playerUUID)
            "player_button_item" -> myCanvas.setSprite(CanvasSprite.DBUTTON_ITEM, col, objName, playerUUID)
            "player_button_mercy" -> myCanvas.setSprite(CanvasSprite.DBUTTON_MERCY, col, objName, playerUUID)
            "player_button_defend" -> myCanvas.setSprite(CanvasSprite.DBUTTON_DEFEND, col, objName, playerUUID)
        }
    }

    val playerOptionsObjectNamesToLift = mutableSetOf<String>()
    fun setupLayout() {
        playerOptionsObjectNamesToLift.clear()
        section("ENEMIES", true) {
            val spritedEnemies = battle.getBattleEnemies().filterIsInstance<SpritedEnemy>()
            for (enemy in spritedEnemies.reversed().withIndex()) {
                myCanvas.drawSprite(
                    320f,
                    20f - (spritedEnemies.size - 1) * 24f + enemy.index * 72f,
                    0f,
                    0f,
                    16,
                    enemy.value.canvasSprites.first(),
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
                    animateSprite(
                        enemy.value.canvasSprites,
                        enemy.value.framesPerSprite,
                        "enemy_${enemy.index}",
                        stoppingCondition = { return@animateSprite !battle.isActive() || !enemy.value.isAlive }
                    )
                }
            }
        }

        section("BG_SLIDER", true) {
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
        }

        section("BATTLE_FIELD_BG", true) {
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
                -514f,
                800f,
                400f,
                32,
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
                31,
                CanvasSprite.SQUARE,
                ShaderTextColor.pure("#2e1e25")
            )
        }

        section("PLAYER_STUFF", true) {
            val dPlayers = battle.getBattlePlayers()
            dPlayers.forEach { dPlayer ->
                dPlayer.player?.let { bukkitPlayer ->
                    section("DEBUG", true) {
                        myCanvas.drawText(
                            0f, 160f, 2f, 2f, 48,
                            Component.text("Debug:"),
                            ShaderTextColor.pure("#ffffff"),
                            TextDisplay.TextAlignment.CENTER,
                            1000,
                            "debug_info",
                            bukkitPlayer
                        )
                    }

                    section("SELECTION_BOX_DECORATION", true) {
                        myCanvas.drawSprite(
                            0f,
                            -115f,
                            100f,
                            38f,
                            62,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#00ffff"),
                            "selection_box_outline",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "selection_box_outline"
                        }

                        myCanvas.drawSprite(
                            0f,
                            -115f,
                            98f,
                            36f,
                            61,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#000000"),
                            "selection_box_inner",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "selection_box_inner"
                        }

                        myCanvas.drawSprite(
                            0f,
                            -96f,
                            98f,
                            18f,
                            59,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#ff0000"),
                            "selection_box_inner2",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "selection_box_inner2"
                        }

                        runInfinite(10) { i, action ->
                            if (!battle.isActive() || !myCanvas.targetPlayers.contains(dPlayer.player)) {
                                action.cancel()
                                return@runInfinite
                            }

                            val objName1 = "player_box_decorative_animation_${UUID.randomUUID()}"
                            myCanvas.drawSprite(
                                -99f,
                                -96f,
                                1f,
                                18f,
                                60,
                                CanvasSprite.SQUARE,
                                ShaderTextColor.pure("#00ffff"),
                                objName1,
                                bukkitPlayer
                            ) {
                                runRepeating(40) { i ->
                                    myCanvas.move(0.5f + i / 60f, 0f, objName1, bukkitPlayer.uniqueId)
                                    val brightness = 1f - (i + 1) / 40f
                                    val col = TextColor.color(0f, brightness, brightness)
                                    myCanvas.setSprite(CanvasSprite.SQUARE, ShaderTextColor.pure(col), objName1, bukkitPlayer.uniqueId)
                                }
                                runLater(41) {
                                    myCanvas.remove(objName1, bukkitPlayer.uniqueId)
                                }
                            }

                            val objName2 = "player_box_decorative_animation_${UUID.randomUUID()}"
                            myCanvas.drawSprite(
                                99f,
                                -96f,
                                1f,
                                18f,
                                60,
                                CanvasSprite.SQUARE,
                                ShaderTextColor.pure("#00ffff"),
                                objName2,
                                bukkitPlayer
                            ) {
                                runRepeating(40) { i ->
                                    myCanvas.move(-0.5f - i / 60f, 0f, objName2, bukkitPlayer.uniqueId)
                                    val brightness = 1f - (i + 1) / 40f
                                    val col = TextColor.color(0f, brightness, brightness)
                                    myCanvas.setSprite(CanvasSprite.SQUARE, ShaderTextColor.pure(col), objName2, bukkitPlayer.uniqueId)
                                }
                                runLater(41) {
                                    myCanvas.remove(objName2, bukkitPlayer.uniqueId)
                                }
                            }
                        }

                        myCanvas.drawSprite(
                            0f,
                            -78f,
                            100f,
                            1f,
                            30,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#00ffff"),
                            player = bukkitPlayer
                        )

                        myCanvas.drawSprite(
                            0f,
                            -78f,
                            98f,
                            1f,
                            29,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#0a2847"),
                            player = bukkitPlayer
                        )

                        myCanvas.drawSprite(
                            0f,
                            -78f,
                            0f,
                            1f,
                            28,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#3990ed"),
                            "selection_box_time_scale",
                            bukkitPlayer
                        )

                        myCanvas.drawSprite(
                            0f,
                            -78f,
                            10f,
                            1f,
                            27,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#000000"),
                            player = bukkitPlayer
                        )

                        myCanvas.drawText(
                            0f,
                            -83f,
                            1f,
                            1f,
                            26,
                            Component.text("⌚"),
                            ShaderTextColor.pure("#d9d9d9"),
                            player = bukkitPlayer
                        )
                    }

                    section("PLAYER_NAME", true) {
                        myCanvas.drawText(
                            -41f,
                            -93f,
                            1.2f,
                            1.2f,
                            57,
                            Component.text("                       \n${dPlayer.player!!.name}"),
                            ShaderTextColor.pure("#ffffff"),
                            TextDisplay.TextAlignment.LEFT,
                            1000,
                            "player_name",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_name"
                        }

                        myCanvas.drawText(
                            -41f + 1f,
                            -93f - 1f,
                            1.2f,
                            1.2f,
                            58,
                            Component.text("                       \n${bukkitPlayer.name}"),
                            ShaderTextColor.pure("#444444"),
                            TextDisplay.TextAlignment.LEFT,
                            1000,
                            "player_name_shadow",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_name_shadow"
                        }
                    }

                    section("HEALTH_BAR", true) {
                        myCanvas.drawText(
                            72f,
                            -110f,
                            1.2f,
                            1.2f,
                            58,
                            Component.text("                       \nHP"),
                            ShaderTextColor.pure("#ffffff"),
                            TextDisplay.TextAlignment.LEFT,
                            1000,
                            "player_hp_text",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_hp_text"
                        }

                        myCanvas.drawSprite(
                            63f,
                            -103f,
                            28f,
                            4f,
                            58,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#6b0e19"),
                            "player_hp_bar_red",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_hp_bar_red"
                        }

                        val offsetPercentage = dPlayer.hp.toFloat() / dPlayer.maxhp.toFloat()
                        myCanvas.drawSprite(
                            35f + offsetPercentage * 28f,
                            -103f,
                            offsetPercentage * 28f,
                            4f,
                            57,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#1bf230"),
                            "player_hp_bar_green",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_hp_bar_green"
                        }

                        myCanvas.drawText(
                            36f,
                            -97f,
                            1.2f,
                            1.2f,
                            58,
                            Component.text("                       \n${dPlayer.hp} / ${dPlayer.maxhp}"),
                            ShaderTextColor.pure("#ffffff"),
                            TextDisplay.TextAlignment.RIGHT,
                            1000,
                            "player_hp_counter",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_hp_counter"
                        }
                    }

                    section("BUTTONS", true) {
                        val buttonColor = ShaderTextColor.pure("#ffffff")

                        myCanvas.drawSprite(
                            -70f,
                            -132.5f,
                            0.5f,
                            0.5f,
                            57,
                            CanvasSprite.DBUTTON_FIGHT,
                            buttonColor,
                            "player_button_fight",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_button_fight"
                        }

                        myCanvas.drawSprite(
                            -35f,
                            -132.5f,
                            0.5f,
                            0.5f,
                            57,
                            CanvasSprite.DBUTTON_ACT,
                            buttonColor,
                            "player_button_act",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_button_act"
                        }

                        myCanvas.drawSprite(
                            0f,
                            -132.5f,
                            0.5f,
                            0.5f,
                            57,
                            CanvasSprite.DBUTTON_ITEM,
                            buttonColor,
                            "player_button_item",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_button_item"
                        }

                        myCanvas.drawSprite(
                            35f,
                            -132.5f,
                            0.5f,
                            0.5f,
                            57,
                            CanvasSprite.DBUTTON_MERCY,
                            buttonColor,
                            "player_button_mercy",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_button_mercy"
                        }

                        myCanvas.drawSprite(
                            70f,
                            -132.5f,
                            0.5f,
                            0.5f,
                            57,
                            CanvasSprite.DBUTTON_DEFEND,
                            buttonColor,
                            "player_button_defend",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_button_defend"
                        }
                    }
                }
            }
        }
    }
}