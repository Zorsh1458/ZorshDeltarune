package dev.zorsh.zorshDeltarune.battle

import dev.zorsh.zorshDeltarune.ZorshDeltarune
import dev.zorsh.zorshDeltarune.animations.BattleBox
import dev.zorsh.zorshDeltarune.nms.FakeTextDisplay
import dev.zorsh.zorshDeltarune.utils.*
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import net.kyori.adventure.title.Title.title
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f
import java.time.Duration
import kotlin.math.pow

class DefaultBattle(players: List<DeltarunePlayer>, enemies: List<DeltaruneEnemy>) : DeltaruneBattle(players, enemies) {

    private var loopTask: BukkitTask? = null

    private var battleJob: Job? = null

    private val battleBox = BattleBox()

    private var playersTurn = false

    private lateinit var background: FakeTextDisplay

    override fun destroyBattle() {
        super.destroyBattle()
        if (loopTask?.isCancelled == false) {
            loopTask?.cancel()
        }
        if (battleJob?.isCancelled == false) {
            battleJob?.cancel()
        }
    }

    override fun startBattle() {
        for (pl in players) {
            pl.lockInBattle(battleCenterLocation)
            pl.player?.showTitle(title(
                fontText("\uD701", "#000000", "space:default"),
                Component.text(""),
                Times.times(Duration.ZERO, Duration.ofMillis(500), Duration.ofMillis(100))
            ))
            pl.player?.playSound(pl.player!!, "encounter", 1f, 1f)
            runLater(10) {
                pl.player?.playSound(pl.player!!, "weaponpull", 1f, 1f)
            }
            runLater(20) {
                pl.player?.playSound(pl.player!!, "battle", 1f, 1f)
            }
        }

        prepareSprites()

        CoroutineScope(Dispatchers.IO).launch {
            val job = scope.launch {
                repeat(2) {
                    delay(100)
                    showPlayersOptions()
                    delay(5000L)
                    playersTurn = false
                    hidePlayersOptions()
                    battleBoxOpen()
                    val jobs = mutableListOf<Job>()
                    for (enemy in enemies) {
                        jobs += launch {
                            enemy.attack()
                        }
                    }
                    jobs.joinAll()
                    delay(200L)
                    battleBoxClose()
                    delay(200L)
                }
            }
            battleJob = job
            job.join()
            end()
        }

        loopTask = object : BukkitRunnable() {
            override fun run() {
                if (players.all { !it.locked }) {
                    if (battleJob?.isCancelled == false) {
                        battleJob?.cancel()
                    }
                    cancel()
                }
            }
        }.runTaskTimer(ZorshDeltarune.instance, 1L, 1L)
    }

    override fun end() {
        super.end()
    }

    private suspend fun battleBoxOpen() {
        battleBox.openAnimation()
        delay(1500)
    }

    private suspend fun battleBoxClose() {
        battleBox.closeAnimation()
        delay(1500)
    }

    private suspend fun showPlayersOptions() {
        repeat(2) {
            for (dPlayer in players) {
                for (entity in dPlayer.perPlayerEntities) {
                    val transform = entity.transformation
                    entity.changeTransformation(
                        Transformation(
                            transform.translation + Vector3f(0f, 0.44f, 0f),
                            transform.leftRotation,
                            transform.scale,
                            transform.rightRotation
                        )
                    )
                }
            }
            delay(50)
        }
        playersTurn = true
    }

    private suspend fun hidePlayersOptions() {
        repeat(2) {
            for (dPlayer in players) {
                for (entity in dPlayer.perPlayerEntities) {
                    val transform = entity.transformation
                    entity.changeTransformation(
                        Transformation(
                            transform.translation + Vector3f(0f, -0.44f, 0f),
                            transform.leftRotation,
                            transform.scale,
                            transform.rightRotation
                        )
                    )
                }
            }
            delay(50)
        }
        playersTurn = false
    }

    private fun prepareSprites() {
        val loc = battleBoxCenterLocation
        loc.yaw = 180f

        newTextDisplay(
            battleBoxCenterLocation + Vector3f(0f, 1.5f, 0f),
            Component.text("\uE201-").font("space:dsprites").color("#00aa00"),
            data = FakeDisplayData(
                Transformation(
                    Vector3f(0f),
                    Quaternionf(0f, 0f, 0f, 1f),
                    Vector3f(0f),
                    Quaternionf(0f, 0f, 0f, 1f)
                )
            )
        ) { entity ->
            battleBox.outerPart = entity
        }

        newTextDisplay(
            battleBoxCenterLocation + Vector3f(0f, 1.5f, 0f),
            Component.text("\uE201-").font("space:dsprites").color("#000000"),
            data = FakeDisplayData(
                Transformation(
                    Vector3f(0f),
                    Quaternionf(0f, 0f, 0f, 1f),
                    Vector3f(0f),
                    Quaternionf(0f, 0f, 0f, 1f)
                )
            )
        ) { entity ->
            battleBox.innerPart = entity
        }

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#ff770a]>⬛;background_color=<color[#ffffff].with_alpha[0]>;scale=140,100,1;translation=-2,-10,-0.001;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#ff4f80"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -10f, -0.202f),
                Quaternionf(0f, 0f, 0f, 1f),
                Vector3f(140f, 100f, 1f),
                Quaternionf(0f, 0f, 0f, 1f)
            ))
        ) { entity ->
            background = entity
        }

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#000000]>⬛;background_color=<color[#ffffff].with_alpha[0]>;left_rotation=<location[0,0,1].to_axis_angle_quaternion[3.1415]>;scale=180,100,1;translation=-2,2.49,-0.0009;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#000000"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, 2.49f, -0.0009f),
                AxisAngle4f(3.1415f, 0f, 0f, 1f),
                Vector3f(180f, 100f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#000000]>⬛;background_color=<color[#ffffff].with_alpha[0]>;left_rotation=<location[0,0,1].to_axis_angle_quaternion[3.1415]>;scale=180,100,1;translation=-2,1.49,0.01;force_no_persist=true] <[pos].forward[5].face[<[pos]>]> save:main_bg
        newTextDisplay(
            loc,
            coloredText("⬛", "#000000"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, 1.5f, 0.01f),
                AxisAngle4f(3.1415f, 0f, 0f, 1f),
                Vector3f(180f, 100f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#2e1e25]>-;background_color=<color[#ffffff].with_alpha[0]>;scale=160,2.2,1;translation=-2,-1.5225,0;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.00095]> save:bg
        newTextDisplay(
            loc - Vector3d(0.0, 0.0, 0.00095),
            coloredText("-", "#2e1e25"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -1.5225f, 0f),
                AxisAngle4f(),
                Vector3f(200f, 2.2f, 1f),
                AxisAngle4f()
            ))
        )

        //- spawn text_display[brightness=<map[block=15;sky=15]>;text=<&color[#2e1e25]>-;background_color=<color[#ffffff].with_alpha[0]>;scale=160,2.2,1;translation=-2,-2.525,0.001;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.00095]> save:bg
        newTextDisplay(
            loc - Vector3d(0.0, 0.0, 0.00095),
            coloredText("-", "#2e1e25"),
            data = FakeDisplayData(Transformation(
                Vector3f(-2f, -2.525f, 0.001f),
                AxisAngle4f(),
                Vector3f(200f, 2.2f, 1f),
                AxisAngle4f()
            ))
        )

        for (dPlayer in players.filter { it.player != null }) {
            dPlayer.perPlayerEntities = mutableListOf()
            dPlayer.playerButtonTexts = mutableListOf()
            dPlayer.playerSelectedButton = 0
            dPlayer.onLeftPressed {
                if (dPlayer.playerSelectedButton > 0) {
                    val newIndex = dPlayer.playerSelectedButton - 1
                    val buttonEntity1 = dPlayer.playerButtonTexts[newIndex+1]
                    buttonEntity1.changeTransformation(Transformation(
                        buttonEntity1.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(0f, 1f, 1f),
                        AxisAngle4f()
                    ))
                    val buttonEntity2 = dPlayer.playerButtonTexts[newIndex]
                    buttonEntity2.changeTransformation(Transformation(
                        buttonEntity2.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(1f, 1f, 1f),
                        AxisAngle4f()
                    ))
                    dPlayer.playerSelectedButton = newIndex
                }
            }
            dPlayer.onRightPressed {
                if (dPlayer.playerSelectedButton < 4) {
                    val newIndex = dPlayer.playerSelectedButton + 1
                    val buttonEntity1 = dPlayer.playerButtonTexts[newIndex-1]
                    buttonEntity1.changeTransformation(Transformation(
                        buttonEntity1.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(0f, 1f, 1f),
                        AxisAngle4f()
                    ))
                    val buttonEntity2 = dPlayer.playerButtonTexts[newIndex]
                    buttonEntity2.changeTransformation(Transformation(
                        buttonEntity2.transformation.translation,
                        AxisAngle4f(),
                        Vector3f(1f, 1f, 1f),
                        AxisAngle4f()
                    ))
                    dPlayer.playerSelectedButton = newIndex
                }
            }

            val mcPlayer = dPlayer.player

            //- spawn item_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;item=<[pl].skull_item>;right_rotation=<[rr]>;scale=1,1,0.005;translation=<[left_offset].add[0.65]>,-1.5,0.003;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            val item = ItemStack.of(Material.PLAYER_HEAD)
            val meta = item.itemMeta as SkullMeta
            meta.setOwningPlayer(mcPlayer)
            item.setItemMeta(meta)
            val rightRotation = Quaternionf(AxisAngle4f(0.5f, 1f, 0f, 0f)) *
                    Quaternionf(AxisAngle4f(225f / 180f * 3.1415f, 0f, 1f, 0f))
            val name = mcPlayer?.name ?: ""
            val actualWidth = name.length * 0.14f
            val textWidth = 0.7f
            val width = textWidth * 0.9f
            val leftOffset = -width - 1.95f
            newItemDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                item,
                playerToShow = listOfNotNull(mcPlayer),
                data = FakeDisplayData(
                    Transformation(
                        Vector3f(leftOffset + 0.65f, -1.5f, 0.003f),
                        Quaternionf(AxisAngle4f()),
                        Vector3f(1f, 1f, 0.005f),
                        rightRotation
                    )
                )
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]><[n].font[space:smooth2]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1.8,2.5,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.15]>]>,-2.15,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                fontText(name, "#e3e3e3", "space:smooth2"),
                data = FakeDisplayData(Transformation(
                    Vector3f(width + leftOffset + 1.15f, -2.15f - (1f - (actualWidth / 0.7f).pow(0.5f)) * 0.25f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(1.8f / actualWidth * 0.7f, 2.5f / (actualWidth / 0.7f).pow(0.5f), 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#e3e3e3]>HP;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1.1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[1.55]>]>,-2.1,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                coloredText("HP", "#e3e3e3"),
                data = FakeDisplayData(Transformation(
                    Vector3f(width * 2f + leftOffset + 1.55f, -2.1f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(1f, 1.1f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<&color[#fc8403]><element[f].font[space:dbuttons]> <element[a].font[space:dbuttons]> <element[i].font[space:dbuttons]> <element[m].font[space:dbuttons]> <element[d].font[space:dbuttons]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1,1,1;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[0.9].add[<[left_offset].add[1.9]>]>,-3.02,0.00021;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            val buttons = fontText("f a i m d", "#fe0001", "space:dbuttons")
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                buttons,
                data = FakeDisplayData(Transformation(
                    Vector3f(width + leftOffset + 1.9f, -3f, 0.00021f),
                    AxisAngle4f(),
                    Vector3f(1f, 1f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
                dPlayer.playerButtons = entity
            }

            val separator = Component.text("\uF801").font("space:default")
            val buttonTexts = listOf(
                Component.text("Б").append(separator)
                    .append(Component.text("и").append(separator))
                    .append(Component.text("т").append(separator))
                    .append(Component.text("в").append(separator))
                    .append(Component.text("а").append(separator))
                    .color("#ff7701"),
                Component.text("Д").append(separator)
                    .append(Component.text("е").append(separator))
                    .append(Component.text("и").append(separator))
                    .append(Component.text("с").append(separator))
                    .append(Component.text("т").append(separator))
                    .append(Component.text(".").append(separator))
                    .color("#ff7701"),
                Component.text("П").append(separator)
                    .append(Component.text("р").append(separator))
                    .append(Component.text("е").append(separator))
                    .append(Component.text("д").append(separator))
                    .append(Component.text("м").append(separator))
                    .append(Component.text(".").append(separator))
                    .color("#ff7701"),
                Component.text("П").append(separator)
                    .append(Component.text("о").append(separator))
                    .append(Component.text("щ").append(separator))
                    .append(Component.text("а").append(separator))
                    .append(Component.text("д").append(separator))
                    .append(Component.text("а").append(separator))
                    .color("#ff7701"),
                Component.text("З").append(separator)
                    .append(Component.text("а").append(separator))
                    .append(Component.text("щ").append(separator))
                    .append(Component.text("и").append(separator))
                    .append(Component.text("т").append(separator))
                    .append(Component.text("а").append(separator))
                    .color("#ff7701")
            )
            var ind = -3
            for (buttonText in buttonTexts) {
                ind++
                newTextDisplay(
                    loc - Vector3d(0.0, 0.0, 0.001),
                    buttonText,
                    data = FakeDisplayData(
                        Transformation(
                            Vector3f(width + leftOffset + 1.9f + ind * 0.9f, -3.12f, 0.00025f),
                            AxisAngle4f(),
                            Vector3f(0f, 1f, 1f),
                            AxisAngle4f()
                        )
                    )
                ) { entity ->
                    dPlayer.perPlayerEntities.add(entity)
                    dPlayer.playerButtonTexts.add(entity)
                }
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<[text]>;background_color=<color[#ffffff].with_alpha[0]>;scale=2,4.7,1;translation=<[left_offset].sub[0.05]>,-3.3,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            var text = ghostEffect(Component.text(",").font("space:default"), 5, "80")
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                text,
                data = FakeDisplayData(Transformation(
                    Vector3f(leftOffset - 0.05f, -3.3f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(2f, 4.7f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<[text]>;background_color=<color[#ffffff].with_alpha[0]>;scale=2,4.69,1;translation=<[left_offset].add[0.075].mul[-0.95]>,-3.3,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            text = ghostEffect(Component.text(",").font("space:default"), 5, "00")
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                text,
                data = FakeDisplayData(Transformation(
                    Vector3f((leftOffset + 0.075f) * -0.95f, -3.3f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(2f, 4.69f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<&color[#00ffff]>⬛;background_color=<color[#ffffff].with_alpha[0]>;scale=<[sx1]>,<[sy1]>,1;translation=<location[<element[<[sx1]>].mul[0.075]>,<element[<[sy1]>].mul[-0.0365]>,0].add[<[left_offset]>,-3.2,0.00005]>;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            val sx1 = textWidth + 28.2f
            val sy1 = 6f
            val sx2 = textWidth + 27.6f
            val sy2 = 5.4f
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                coloredText("⬛", "#00ffff"),
                data = FakeDisplayData(Transformation(
                    Vector3f(sx1 * 0.075f + leftOffset, sy1 * -0.0365f - 2.25f, 0.00005f),
                    AxisAngle4f(),
                    Vector3f(sx1, sy1, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                coloredText("⬛", "#000000"),
                data = FakeDisplayData(Transformation(
                    Vector3f(sx1 * 0.0753f + leftOffset, sy2 * -0.0365f - 2.2f, 0.0001f),
                    AxisAngle4f(),
                    Vector3f(sx2, sy2, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //TODO("HEALTHBAR")
            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<&color[<[pl_col]>]><&n><element[ ].repeat[<[hp_max]>]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=<element[16].div[<[hp_max]>]>,10,1;line_width=10000;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[2.6]>]>,-2.07,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                Component.text(" ".repeat(dPlayer.maxhp)).style(Style.style(TextDecoration.UNDERLINED)).color("#00ff00"),
                data = FakeDisplayData(Transformation(
                    Vector3f(textWidth * 1.8f + leftOffset + 2.6f, -2f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(0.16f, 10f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }

            //        - spawn text_display[teleport_duration=<[tp_dur]>;brightness=<map[block=15;sky=15]>;text=<element[<[hp]> / <[hp_max]>]>;background_color=<color[#ffffff].with_alpha[0]>;scale=1.2,1.2,1;line_width=10000;translation=<[n].font[space:smooth2].text_width.mul[0.025].mul[1.8].add[<[left_offset].add[2.6]>]>,-1.7,0.00015;force_no_persist=true] <[pos].forward[5].face[<[pos]>].forward[0.001]> save:bg
            newTextDisplay(
                loc - Vector3d(0.0, 0.0, 0.001),
                Component.text("${dPlayer.hp} / ${dPlayer.maxhp}"),
                data = FakeDisplayData(Transformation(
                    Vector3f(textWidth * 1.8f + leftOffset + 2.6f, -1.7f, 0.00015f),
                    AxisAngle4f(),
                    Vector3f(1.2f, 1.2f, 1f),
                    AxisAngle4f()
                ))
            ) { entity ->
                dPlayer.perPlayerEntities.add(entity)
            }
        }
    }
}
