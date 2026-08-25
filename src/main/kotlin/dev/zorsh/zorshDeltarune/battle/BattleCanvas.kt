package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ui.CanvasSprite
import dev.zorsh.zorshDeltarune.ui.PlayerUICanvas
import dev.zorsh.zorshDeltarune.utils.runLater
import dev.zorsh.zorshDeltarune.utils.runRepeating
import org.bukkit.entity.Player

class BattleCanvas(val players: List<Player>, val battle: INeverlandBattle) {
    val myCanvas = PlayerUICanvas()

    fun initCanvas() {
        myCanvas.initialize(players)
    }

    fun setupLayout() {

        val spritedEnemies = battle.getBattleEnemies().filterIsInstance<SpritedEnemy>()
        for (enemy in spritedEnemies.reversed().withIndex()) {
            myCanvas.drawSprite(
                200f,
                100f - (spritedEnemies.size - 1) * 6f + enemy.index * 18f,
                2f,
                2f,
                16,
                enemy.value.canvasSprite,
                "#ffffff",
                "enemy_${enemy.index}",
                player = null
            ) {
                runLater(10 + enemy.index.toLong() * 2L) {
                    runRepeating(10) { _ ->
                        myCanvas.move(-16f, 0f, "enemy_${enemy.index}")
                    }
                }
            }
        }

        myCanvas.drawSprite(
            0f,
            0f,
            800f,
            800f,
            128,
            CanvasSprite.SQUARE,
            "#fd0100",
            null,
            null
        )

        myCanvas.drawSprite(
            0f,
            -460f,
            800f,
            400f,
            64,
            CanvasSprite.SQUARE,
            "#000000",
            null,
            null
        )

        myCanvas.drawSprite(
            0f,
            -60f,
            800f,
            1f,
            63,
            CanvasSprite.SQUARE,
            "#2e1e25",
            null,
            null
        )

        myCanvas.drawSprite(
            0f,
            -96f,
            800f,
            1f,
            63,
            CanvasSprite.SQUARE,
            "#2e1e25",
            null,
            null
        )

        myCanvas.drawSprite(
            0f,
            0f,
            240f,
            240f,
            60,
            CanvasSprite.SQUARE,
            "#ffffff",
            null,
            null
        )

        myCanvas.drawSprite(
            0f,
            0f,
            238f,
            238f,
            59,
            CanvasSprite.SQUARE,
            "#000000",
            null,
            null
        )

        val dPlayers = battle.getBattlePlayers().filter { it.player != null && it.player?.isOnline == true }
        for (dPlayer in dPlayers) {
//            dPlayer.playerSelectedButton = 0

//            val mcPlayer = dPlayer.player


            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]><[n].font[space:smooth2]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1.8,2.5,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.15]>]>,-2.15,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                fontText(name, "#e3e3e3", "space:smooth2"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(
//                            width + leftOffset + 1.15f,
//                            -2.15f - (1f - (actualWidth / 0.7f).pow(0.5f)) * 0.25f,
//                            0.00015f
//                        ) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1.8f / actualWidth * 0.7f, 2.5f / (actualWidth / 0.7f).pow(0.5f), 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]>HP;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1.1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[1.55]>]>,-2.1,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                coloredText("HP", "#e3e3e3"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(width * 2f + leftOffset + 1.55f, -2.03f, 0.00015f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1f, 1.1f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<&color[#fc8403]><element[f].font[space:dbuttons]> <element[a].font[space:dbuttons]> <element[i].font[space:dbuttons]> <element[m].font[space:dbuttons]> <element[d].font[space:dbuttons]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.9]>]>,-3.02,0.00021;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
//            val buttons = fontText("f a i m d", "#ff8800", "space:dbuttons")
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                buttons,
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(width + leftOffset + 1.9f, -3f, 0.00021f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1f, 1f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//                dPlayer.playerButtons = entity
//            }
//
//            val separator = Component.text("\uF801").font("space:default")
//            val buttonTexts = listOf(
//                fontText("f\n", "#ffff00", "space:dbuttons")
//                    .append(Component.text("Б").font("minecraft:default").append(separator))
//                    .append(Component.text("и").font("minecraft:default").append(separator))
//                    .append(Component.text("т").font("minecraft:default").append(separator))
//                    .append(Component.text("в").font("minecraft:default").append(separator))
//                    .append(Component.text("а").font("minecraft:default").append(separator))
//                    .color("#ffff00"),
//                fontText("a\n", "#ffff00", "space:dbuttons")
//                    .append(Component.text("Д").font("minecraft:default").append(separator))
//                    .append(Component.text("е").font("minecraft:default").append(separator))
//                    .append(Component.text("и").font("minecraft:default").append(separator))
//                    .append(Component.text("с").font("minecraft:default").append(separator))
//                    .append(Component.text("т").font("minecraft:default").append(separator))
//                    .append(Component.text(".").font("minecraft:default").append(separator))
//                    .color("#ffff00"),
//                fontText("i\n", "#ffff00", "space:dbuttons")
//                    .append(Component.text("П").font("minecraft:default").append(separator))
//                    .append(Component.text("р").font("minecraft:default").append(separator))
//                    .append(Component.text("е").font("minecraft:default").append(separator))
//                    .append(Component.text("д").font("minecraft:default").append(separator))
//                    .append(Component.text("м").font("minecraft:default").append(separator))
//                    .append(Component.text(".").font("minecraft:default").append(separator))
//                    .color("#ffff00"),
//                fontText("m\n", "#ffff00", "space:dbuttons")
//                    .append(Component.text("П").font("minecraft:default").append(separator))
//                    .append(Component.text("о").font("minecraft:default").append(separator))
//                    .append(Component.text("щ").font("minecraft:default").append(separator))
//                    .append(Component.text("а").font("minecraft:default").append(separator))
//                    .append(Component.text("д").font("minecraft:default").append(separator))
//                    .append(Component.text("а").font("minecraft:default").append(separator))
//                    .color("#ffff00"),
//                fontText("d\n", "#ffff00", "space:dbuttons")
//                    .append(Component.text("З").font("minecraft:default").append(separator))
//                    .append(Component.text("а").font("minecraft:default").append(separator))
//                    .append(Component.text("щ").font("minecraft:default").append(separator))
//                    .append(Component.text("и").font("minecraft:default").append(separator))
//                    .append(Component.text("т").font("minecraft:default").append(separator))
//                    .append(Component.text("а").font("minecraft:default").append(separator))
//                    .color("#ffff00")
//            )
//            var ind = -3
//            for (buttonText in buttonTexts) {
//                ind++
//                newTextDisplay(
//                    loc - Vector3d(0.0, 0.0, 0.001),
//                    buttonText,
//                    playerToShow = listOfNotNull(mcPlayer),
//                    data = FakeDisplayData(
//                        Transformation(
//                            Vector3f(
//                                width + leftOffset + 1.9f + ind * 0.9f,
//                                -3.25f + 0.88f,
//                                0.01025f
//                            ) * sceneScale + sceneOffset,
//                            AxisAngle4f(),
//                            Vector3f(0f, 1f, 1f) * sceneScale,
//                            AxisAngle4f()
//                        ),
//                        interpolationDuration = 0
//                    ),
//                    mountTo = true
//                ) { entity ->
////                    dPlayer.perPlayerEntities.add(entity)
//                    dPlayer.playerButtonTexts.add(entity)
//                }
//            }
//            dPlayer.onJumpPressed {
//                proceedPlayerSubmit(dPlayer)
//            }
//            dPlayer.onLeftPressed {
//                if (dPlayer.playerSelectedButton > 0 && playersTurn && dPlayer.actionStage == PlayerActionStage.SELECT_BUTTON) {
//                    val newIndex = dPlayer.playerSelectedButton - 1
//                    val buttonEntity1 = dPlayer.playerButtonTexts[newIndex + 1]
//                    buttonEntity1.changeTransformation(
//                        Transformation(
//                            buttonEntity1.transformation.translation,
//                            AxisAngle4f(),
//                            Vector3f(0f, 1f, 1f) * sceneScale,
//                            AxisAngle4f()
//                        )
//                    )
//                    val buttonEntity2 = dPlayer.playerButtonTexts[newIndex]
//                    buttonEntity2.changeTransformation(
//                        Transformation(
//                            buttonEntity2.transformation.translation,
//                            AxisAngle4f(),
//                            Vector3f(1f, 1f, 1f) * sceneScale,
//                            AxisAngle4f()
//                        )
//                    )
//                    dPlayer.playerSelectedButton = newIndex
//                }
//            }
//            dPlayer.onRightPressed {
//                if (dPlayer.playerSelectedButton < 4 && playersTurn && dPlayer.actionStage == PlayerActionStage.SELECT_BUTTON) {
//                    val newIndex = dPlayer.playerSelectedButton + 1
//                    val buttonEntity1 = dPlayer.playerButtonTexts[newIndex - 1]
//                    buttonEntity1.changeTransformation(
//                        Transformation(
//                            buttonEntity1.transformation.translation,
//                            AxisAngle4f(),
//                            Vector3f(0f, 1f, 1f) * sceneScale,
//                            AxisAngle4f()
//                        )
//                    )
//                    val buttonEntity2 = dPlayer.playerButtonTexts[newIndex]
//                    buttonEntity2.changeTransformation(
//                        Transformation(
//                            buttonEntity2.transformation.translation,
//                            AxisAngle4f(),
//                            Vector3f(1f, 1f, 1f) * sceneScale,
//                            AxisAngle4f()
//                        )
//                    )
//                    dPlayer.playerSelectedButton = newIndex
//                }
//            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#00ffff]>⬛;background_color=<color[#ffffff].with_alpha[0]>;scale=<[sx1]>,<[sy1]>,1;translation=<location[<element[<[sx1]>].mul[0.075]>,<element[<[sy1]>].mul[-0.0365]>,0].add[<[left_offset]>,-3.2,0.00005]>;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
//            val sx1 = textWidth + 28.2f
//            val sy1 = 6f
//            val sx2 = textWidth + 27.6f
//            val sy2 = 5.4f
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                coloredText("⬛", "#00ffff"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(
//                            sx1 * 0.075f + leftOffset,
//                            sy1 * -0.0365f - 2.25f,
//                            0.00005f
//                        ) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(sx1, sy1, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//            }
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                coloredText("⬛", "#000000"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(sx1 * 0.0753f + leftOffset, sy2 * -0.0365f - 2.2f, 0.0001f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(sx2, sy2, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//            }

//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                Component.text(" ".repeat(dPlayer.maxhp)).style(Style.style(TextDecoration.UNDERLINED))
//                    .color("#00ff00"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(textWidth * 1.8f + leftOffset + 2.6f, -2f, 0.00015f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(16f / dPlayer.maxhp, 10f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//                dPlayer.healthBar = entity
//            }
//
//            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<[hp]> / <[hp_max]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1.2,1.2,1;line_width=10000;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[2.6]>]>,-1.7,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
//            newTextDisplay(
//                loc - Vector3d(0.0, 0.0, 0.001),
//                Component.text("${dPlayer.hp} / ${dPlayer.maxhp}"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(textWidth * 1.8f + leftOffset + 2.6f, -1.7f, 0.00015f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1.2f, 1.2f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.perPlayerEntities.add(entity)
//                dPlayer.healthCounter = entity
//            }

//            val tpBarPos = Vector3d(6.0, 2.0, -0.001)
//            newTextDisplay(
//                loc + tpBarPos,
//                Component.text(" ".repeat(100)).style(Style.style(TextDecoration.UNDERLINED)).color("#770000"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(-6f, 2f, 0.00015f - 0.001f) * sceneScale + sceneOffset,
//                        AxisAngle4f(1.5708f, 0.0f, 0.0f, 1.0f),
//                        Vector3f(0.32f, 20f * 0.75f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.tpBar = entity
//            }
//            newTextDisplay(
//                loc + tpBarPos + Vector3d(0.19, -0.057, -0.01),
//                Component.text("\uD702").font("space:default"),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(-6.19f, 2.0f - 0.057f, 0.00018f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1.1f * 0.8f, 1.14f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            )
//
//            newTextDisplay(
//                loc + tpBarPos + Vector3d(0.18, 1.8, 0.0),
//                Component.text("0").font("space:smooth").append(
//                    Component.text("%").font("minecraft:default")
//                ),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(
//                    Transformation(
//                        Vector3f(-6.18f, 3.8f, 0.00015f) * sceneScale + sceneOffset,
//                        AxisAngle4f(),
//                        Vector3f(1.5f, 1.2f, 1f) * sceneScale,
//                        AxisAngle4f()
//                    )
//                ),
//                mountTo = true
//            ) { entity ->
//                dPlayer.tpCounter = entity
//            }

//            newTextDisplay(
//                loc,
//                Component.text("0").font("space:smooth").append(
//                    Component.text("%").font("minecraft:default")
//                ),
//                playerToShow = listOfNotNull(mcPlayer),
//                data = FakeDisplayData(Transformation(
//                    Vector3f(-6.18f, 4.2f, 0.00015f) * sceneScale + sceneOffset,
//                    AxisAngle4f(),
//                    Vector3f(1.5f, 1.2f, 1f) * sceneScale,
//                    AxisAngle4f()
//                )),
//                mountTo = true
//            ) { entity ->
//                enemies.forEach { enemy ->
//                    if (enemy is TestEnemy) {
//                        enemy.textBeb = entity
//                    }
//                }
//            }
        }
    }
}