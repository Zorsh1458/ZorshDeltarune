package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.battle.enemy.SpritedEnemy
import dev.zorsh.zorshDeltarune.battle.player.PlayerBattleDataStorage
import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import dev.zorsh.zorshDeltarune.ui.ShaderTextColor
import dev.zorsh.zorshDeltarune.utils.plus
import dev.zorsh.zorshDeltarune.utils.runInfinite
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Transformation
import org.joml.Vector3f
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

    lateinit var playerBattleDataStorage: HashMap<UUID, PlayerBattleDataStorage>

    fun createEnemiesList(player: Player) {
        val storage = playerBattleDataStorage[player.uniqueId] ?: return

        val px = -128f
        var py = -140f

        storage.optionsObjectNamesList = mutableListOf()
        storage.optionsListSize = 1 to battle.getBattleEnemies().size
        battle.getBattleEnemies().forEach { enemy ->
            val objName = "enemy_list_entry_${UUID.randomUUID()}"
            val component = Component.text("                              \n⏵ ${enemy.name}")
            myCanvas.drawText(
                px, py, 1.5f, 1.5f, 16, component, ShaderTextColor.pure("#ffffff"),
                alignment = TextDisplay.TextAlignment.LEFT,
                saveAs = objName,
                player = player
            ) {
                storage.optionsObjectNamesList.add(objName)
            }
            py -= 20f
        }
    }

    fun clearEnemiesList(playerUUID: UUID) {
        val storage = playerBattleDataStorage[playerUUID] ?: return
        storage.optionsObjectNamesList.forEach { objName ->
            myCanvas.remove(objName, playerUUID)
        }
        storage.optionsObjectNamesList.clear()
    }

    fun createOptionsSelector(player: Player) {
        val storage = playerBattleDataStorage[player.uniqueId] ?: return
        storage.optionsSelectorPosition = 1 to 1
        myCanvas.drawSprite(
            -232f, -133f, 0.75f, 0.75f, 16,
            CanvasSprite.SOUL,
            ShaderTextColor.pure("#ff0000"),
            "menu_selector",
            player
        )
    }

    fun moveOptionsSelectorLeft(playerUUID: UUID) {
        val storage = playerBattleDataStorage[playerUUID] ?: return
        val pos = storage.optionsSelectorPosition
        if (pos.first <= 1) return
        storage.optionsSelectorPosition = pos.first - 1 to pos.second
        myCanvas.move(-200f, 0f, "menu_selector", playerUUID)
    }

    fun moveOptionsSelectorRight(playerUUID: UUID) {
        val storage = playerBattleDataStorage[playerUUID] ?: return
        val pos = storage.optionsSelectorPosition
        val size = storage.optionsListSize
        if (pos.first >= size.first) return
        storage.optionsSelectorPosition = pos.first + 1 to pos.second
        myCanvas.move(200f, 0f, "menu_selector", playerUUID)
    }

    fun moveOptionsSelectorUp(playerUUID: UUID) {
        val storage = playerBattleDataStorage[playerUUID] ?: return
        val pos = storage.optionsSelectorPosition
        if (pos.second <= 1) return
        storage.optionsSelectorPosition = pos.first to pos.second - 1
        myCanvas.move(0f, 20f, "menu_selector", playerUUID)
    }

    fun moveOptionsSelectorDown(playerUUID: UUID) {
        val storage = playerBattleDataStorage[playerUUID] ?: return
        val pos = storage.optionsSelectorPosition
        val size = storage.optionsListSize
        if (pos.second >= size.second) return
        storage.optionsSelectorPosition = pos.first to pos.second + 1
        myCanvas.move(0f, -20f, "menu_selector", playerUUID)
    }

    fun removeOptionsSelector(playerUUID: UUID) {
        myCanvas.remove("menu_selector", playerUUID)
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

    fun startAttackUI(player: Player) {
        val storage = playerBattleDataStorage[player.uniqueId]
        storage?.let { st ->
            st.selectedEnemy = battle.getBattleEnemies()[st.optionsSelectorPosition.second - 1]
            st.selectedEnemyIndex = st.optionsSelectorPosition.second - 1
        }

        myCanvas.drawSprite(
            -128f, -132f,
            72f, 16f,
            24,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#38090c"),
            "attack_box_outer",
            player
        )
        myCanvas.drawSprite(
            -126f, -132f,
            72f, 14f,
            23,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000"),
            "attack_box_inner",
            player
        )
        myCanvas.drawSprite(
            -195f, -132f,
            5f, 16f,
            22,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#ffff00"),
            "attack_box_crit_zone_outer",
            player
        )
        myCanvas.drawSprite(
            -195f, -132f,
            3f, 14f,
            21,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#000000"),
            "attack_box_crit_zone_inner",
            player
        )
        myCanvas.drawSprite(
            6f, -132f,
            3f, 14f,
            20,
            CanvasSprite.SQUARE,
            ShaderTextColor.pure("#ffffff"),
            "attack_box_damage_indicator",
            player
        ) {
            runRepeating(22) { i ->
                myCanvas.move(-10f, 0f, "attack_box_damage_indicator", player.uniqueId)

                if (i % 2 == 1) {
                    val objName = "attack_box_damage_indicator_shadow_${UUID.randomUUID()}"
                    myCanvas.drawSprite(
                        6f + (0.75f - i) * 10f, -132f,
                        3f, 14f,
                        20,
                        CanvasSprite.SQUARE,
                        ShaderTextColor.pure("#ffffff"),
                        objName,
                        player
                    ) {
                        runRepeating(8) { t ->
                            val brightness = 1f - (t + 1) / 8f
                            myCanvas.setSprite(
                                CanvasSprite.SQUARE,
                                ShaderTextColor.pure(TextColor.color(brightness, brightness, brightness)),
                                objName,
                                player.uniqueId
                            )
                            myCanvas.setScale(
                                3f - 3f * t / 8f,
                                14f,
                                objName,
                                player.uniqueId
                            )
                        }
                        runLater(9) {
                            myCanvas.remove(objName, player.uniqueId)
                        }
                    }
                }
            }
            runLater(24) {
                myCanvas.remove("attack_box_damage_indicator", player.uniqueId)
            }
        }
    }

    fun confirmAttack(player: Player) {
        val storage = playerBattleDataStorage[player.uniqueId] ?: return
        val ind = storage.selectedEnemyIndex
        val frames = 10
        var counter = 0
        val pos = myCanvas.getPosition("enemy_$ind")
        animateStatusText(pos.first, pos.second, 2.25f, 2f, ShaderTextColor.pure("#ffaaaa"), Component.text(999), player)
        runRepeating(frames) { i ->
            counter = (counter + 1) % 2
            val power = frames - i - 1
            val shift = power * (counter * 2 - 1) * 16f / frames
            myCanvas.setPosition(
                pos.first + shift, pos.second,
                "enemy_$ind"
            )
        }
    }

    fun removeAttackUI(playerUUID: UUID) {
        myCanvas.remove("attack_box_outer", playerUUID)
        myCanvas.remove("attack_box_inner", playerUUID)
        myCanvas.remove("attack_box_crit_zone_outer", playerUUID)
        myCanvas.remove("attack_box_crit_zone_inner", playerUUID)
    }

    fun animateStatusText(px: Float, py: Float, sx: Float, sy: Float, color: ShaderTextColor, text: Component, player: Player? = null) {
        animateStatusText(px, py, sx, sy, 32, color, text, player)
    }

    fun animateStatusText(px: Float, py: Float, sx: Float, sy: Float, z: Int, color: ShaderTextColor, text: Component, player: Player? = null) {
        val objName = "status_text_${UUID.randomUUID()}"
        myCanvas.drawText(
            px, py, sx * 3, 0f, z, text, color,
            saveAs = objName,
            player = player
        ) {
            runRepeating(5) { i ->
                val t = i + 1
                if (i < 3) {
                    myCanvas.setScale(sx * 3 - sx * 2 * t / 3, sy * t / 3, objName, player?.uniqueId)
                }
                myCanvas.move(5f - t, 2.5f - t, objName, player?.uniqueId)
            }
            runLater(30) {
                runRepeating(10) { i ->
                    val t = i + 1
                    myCanvas.setScale(sx - sx * t / 10, sy + sy * 0.5f * t / 10, objName, player?.uniqueId)
                    myCanvas.move(0f, 2f, objName, player?.uniqueId)
                }
            }
            runLater(52) {
                myCanvas.remove(objName, player?.uniqueId)
            }
        }
    }

    val playerOptionsObjectNamesToLift = mutableSetOf<String>()
    fun setupLayout() {
        playerBattleDataStorage = hashMapOf()
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
                playerBattleDataStorage[dPlayer.uuid] = PlayerBattleDataStorage()
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
                            17f,
                            59,
                            CanvasSprite.SQUARE,
                            ShaderTextColor.pure("#000000"),
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
                            -30f,
                            -93f,
                            1.2f,
                            1.2f,
                            57,
                            Component.text("                           \n${bukkitPlayer.name}"),
                            ShaderTextColor.pure("#ffffff"),
                            TextDisplay.TextAlignment.LEFT,
                            1000,
                            "player_name",
                            bukkitPlayer
                        ) {
                            playerOptionsObjectNamesToLift += "player_name"
                        }

                        myCanvas.drawText(
                            -30f + 1f,
                            -93f - 1f,
                            1.2f,
                            1.2f,
                            58,
                            Component.text("                           \n${bukkitPlayer.name}"),
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